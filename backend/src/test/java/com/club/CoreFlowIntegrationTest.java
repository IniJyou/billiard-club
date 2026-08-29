package com.club;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CoreFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void rejectsAnonymousAndInvalidLogins() throws Exception {
        mockMvc.perform(get("/api/tables"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"disabled\",\"password\":\"123456\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cashierCannotChangeMaintenanceStatus() throws Exception {
        MockHttpSession session = login("cashier");
        mockMvc.perform(patch("/api/tables/2/status").session(session)
                        .contentType("application/json")
                        .content("{\"status\":0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void completesMemberRechargeOpenCheckoutAndRecordsFlow() throws Exception {
        MockHttpSession session = login("admin");

        String memberBody = mockMvc.perform(post("/api/members").session(session)
                        .contentType("application/json")
                        .content("{\"name\":\"测试会员\",\"phone\":\"13900000001\",\"levelId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cardNo").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        long memberId = objectMapper.readTree(memberBody).path("data").path("id").asLong();

        mockMvc.perform(post("/api/members/{id}/recharges", memberId).session(session)
                        .contentType("application/json")
                        .content("{\"amount\":100,\"payWay\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newBalance").value(100.0));

        String sessionBody = mockMvc.perform(post("/api/sessions").session(session)
                        .contentType("application/json")
                        .content("{\"tableId\":1,\"memberId\":" + memberId + "}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long tableSessionId = objectMapper.readTree(sessionBody).path("data").path("id").asLong();

        jdbcTemplate.update("UPDATE table_session SET start_time = ? WHERE id = ?",
                LocalDateTime.now().minusMinutes(61), tableSessionId);

        String checkoutBody = mockMvc.perform(post("/api/sessions/{id}/checkout", tableSessionId)
                        .session(session).contentType("application/json")
                        .content("{\"payWay\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bill.durationHours").value(2.0))
                .andExpect(jsonPath("$.data.bill.finalAmount").value(40.0))
                .andExpect(jsonPath("$.data.memberBalance").value(60.0))
                .andReturn().getResponse().getContentAsString();
        JsonNode checkout = objectMapper.readTree(checkoutBody);
        assertThat(checkout.path("data").path("memberPoints").asInt()).isEqualTo(40);

        mockMvc.perform(get("/api/tables").session(session))
                .andExpect(status().isOk());
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM billiard_table WHERE id = 1", Integer.class)).isZero();
        mockMvc.perform(get("/api/records/recharges").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
        mockMvc.perform(get("/api/records/consumptions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void rejectsDuplicatePhone() throws Exception {
        MockHttpSession session = login("admin");
        String body = "{\"name\":\"会员A\",\"phone\":\"13900000002\",\"levelId\":1}";
        mockMvc.perform(post("/api/members").session(session)
                        .contentType("application/json").content(body))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/members").session(session)
                        .contentType("application/json").content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsMaintenanceTableAndDuplicateOpen() throws Exception {
        MockHttpSession session = login("admin");
        mockMvc.perform(post("/api/sessions").session(session)
                        .contentType("application/json").content("{\"tableId\":2}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/sessions").session(session)
                        .contentType("application/json").content("{\"tableId\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/sessions").session(session)
                        .contentType("application/json").content("{\"tableId\":1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void insufficientBalanceRollsBackCheckout() throws Exception {
        MockHttpSession session = login("admin");
        String memberBody = mockMvc.perform(post("/api/members").session(session)
                        .contentType("application/json")
                        .content("{\"name\":\"余额不足会员\",\"phone\":\"13900000003\",\"levelId\":1}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long memberId = objectMapper.readTree(memberBody).path("data").path("id").asLong();

        String sessionBody = mockMvc.perform(post("/api/sessions").session(session)
                        .contentType("application/json")
                        .content("{\"tableId\":1,\"memberId\":" + memberId + "}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long sessionId = objectMapper.readTree(sessionBody).path("data").path("id").asLong();

        mockMvc.perform(post("/api/sessions/{id}/checkout", sessionId).session(session)
                        .contentType("application/json").content("{\"payWay\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("会员余额不足，请充值或改用现金支付"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM billiard_table WHERE id = 1", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM table_session WHERE id = ?", Integer.class, sessionId)).isZero();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_bill", Integer.class)).isZero();
    }

    private MockHttpSession login(String username) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }
}
