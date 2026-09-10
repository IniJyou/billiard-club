package com.club;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserPortalIntegrationTest {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completesRegistrationMembershipRechargeAndReservationFlow() throws Exception {
        String registerBody = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"realName\":\"用户测试\",\"phone\":\"13800000001\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value(3))
                .andReturn().getResponse().getContentAsString();

        MockHttpSession userSession = (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"13800000001\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(get("/api/user/profile").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasMember").value(false));

        mockMvc.perform(get("/api/user/membership-options").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[2].discount").value(0.9))
                .andExpect(jsonPath("$.data[0].pointsThreshold").doesNotExist());

        mockMvc.perform(post("/api/user/membership").session(userSession)
                        .contentType("application/json")
                        .content("{\"name\":\"用户测试\",\"gender\":1,\"birthday\":\"2000-01-01\",\"levelId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasMember").value(true))
                .andExpect(jsonPath("$.data.cardNo").isNotEmpty())
                .andExpect(jsonPath("$.data.levelId").value(3))
                .andExpect(jsonPath("$.data.memberStatus").value(1))
                .andExpect(jsonPath("$.data.discount").value(0.9));

        mockMvc.perform(post("/api/user/recharges").session(userSession)
                        .contentType("application/json")
                        .content("{\"amount\":200,\"giftAmount\":50,\"payWay\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newBalance").value(200.0));

        LocalDateTime start = validStartTime();
        String reservationBody = mockMvc.perform(post("/api/reservations").session(userSession)
                        .contentType("application/json")
                        .content("{\"tableId\":1,\"startTime\":\"" + DATE_TIME.format(start)
                                + "\",\"durationHours\":1,\"remark\":\"测试预约\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(0))
                .andReturn().getResponse().getContentAsString();
        long reservationId = objectMapper.readTree(reservationBody).path("data").path("id").asLong();

        mockMvc.perform(get("/api/reservations/mine").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].reservationNo").isNotEmpty());

        MockHttpSession cashierSession = login("cashier");
        String openBody = mockMvc.perform(post("/api/reservations/{id}/open", reservationId).session(cashierSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(1))
                .andReturn().getResponse().getContentAsString();
        long tableSessionId = objectMapper.readTree(openBody).path("data").path("sessionId").asLong();

        mockMvc.perform(post("/api/sessions/{id}/checkout", tableSessionId).session(cashierSession)
                        .contentType("application/json").content("{\"payWay\":2}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/reservations/mine").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value(2));

        JsonNode registered = objectMapper.readTree(registerBody);
        assertThat(registered.path("data").path("username").asText()).isEqualTo("13800000001");
    }

    @Test
    void enforcesUserRoleSameDayAndOnlinePaymentRules() throws Exception {
        MockHttpSession cashierSession = login("cashier");
        mockMvc.perform(get("/api/user/profile").session(cashierSession))
                .andExpect(status().isForbidden());

        MockHttpSession userSession = registerAndLogin("13800000002");
        mockMvc.perform(get("/api/members").session(userSession))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/user/membership").session(userSession)
                        .contentType("application/json")
                        .content("{\"name\":\"规则测试\",\"gender\":0,\"birthday\":\"2001-02-03\",\"levelId\":1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/user/recharges").session(userSession)
                        .contentType("application/json").content("{\"amount\":100,\"payWay\":1}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/reservations").session(userSession)
                        .contentType("application/json")
                        .content("{\"tableId\":1,\"startTime\":\"2026-09-09T22:00:00\",\"durationHours\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("请求内容或日期格式不正确"));

        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atTime(12, 0);
        mockMvc.perform(post("/api/reservations").session(userSession)
                        .contentType("application/json")
                        .content("{\"tableId\":1,\"startTime\":\"" + DATE_TIME.format(tomorrow)
                                + "\",\"durationHours\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("只能预约当天的球桌"));
    }

    @Test
    void exposesTableStatusAndReservedTimesToEveryRoleWithoutMemberDetails() throws Exception {
        MockHttpSession userSession = registerAndLogin("13800000004");
        mockMvc.perform(post("/api/user/membership").session(userSession)
                        .contentType("application/json")
                        .content("{\"name\":\"预约可见性\",\"gender\":1,\"birthday\":\"2000-01-01\",\"levelId\":1}"))
                .andExpect(status().isOk());
        LocalDateTime start = validStartTime();
        mockMvc.perform(post("/api/reservations").session(userSession)
                        .contentType("application/json")
                        .content("{\"tableId\":1,\"startTime\":\"" + DATE_TIME.format(start)
                                + "\",\"durationHours\":1}"))
                .andExpect(status().isOk());

        for (MockHttpSession session : new MockHttpSession[]{userSession, login("cashier"), login("admin")}) {
            mockMvc.perform(get("/api/tables").session(session))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[?(@.id == 1)].status").value(hasItem(0)))
                    .andExpect(jsonPath("$.data[?(@.id == 1)].todayReservations[0].startTime").exists())
                    .andExpect(jsonPath("$.data[?(@.id == 1)].todayReservations[0].endTime").exists())
                    .andExpect(jsonPath("$.data[?(@.id == 1)].todayReservations[0].memberName").doesNotExist())
                    .andExpect(jsonPath("$.data[?(@.id == 1)].todayReservations[0].userId").doesNotExist());
        }
    }

    @Test
    void allowsOnlyStaffToCancelAndUserToApplyForANewCard() throws Exception {
        MockHttpSession userSession = registerAndLogin("13800000003");
        String membershipBody = mockMvc.perform(post("/api/user/membership").session(userSession)
                        .contentType("application/json")
                        .content("{\"name\":\"注销测试\",\"gender\":2,\"birthday\":\"1999-06-18\",\"levelId\":2}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long memberId = objectMapper.readTree(membershipBody).path("data").path("memberId").asLong();

        mockMvc.perform(post("/api/user/recharges").session(userSession)
                        .contentType("application/json").content("{\"amount\":100,\"payWay\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newBalance").value(100.0));

        MockHttpSession cashierSession = login("cashier");
        mockMvc.perform(post("/api/members/{id}/cancel", memberId).session(cashierSession))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/members").session(cashierSession).param("keyword", "13800000003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(get("/api/user/profile").session(userSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.hasMember").value(false))
                .andExpect(jsonPath("$.data.cardNo").doesNotExist());

        String newMembershipBody = mockMvc.perform(post("/api/user/membership").session(userSession)
                        .contentType("application/json")
                        .content("{\"name\":\"重新办卡\",\"gender\":2,\"birthday\":\"1999-06-18\",\"levelId\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(0.0))
                .andReturn().getResponse().getContentAsString();
        JsonNode newMembership = objectMapper.readTree(newMembershipBody).path("data");
        long newMemberId = newMembership.path("memberId").asLong();
        assertThat(newMemberId).isNotEqualTo(memberId);
        assertThat(newMembership.path("cardNo").asText())
                .isNotEqualTo(objectMapper.readTree(membershipBody).path("data").path("cardNo").asText());

        MockHttpSession adminSession = login("admin");
        mockMvc.perform(post("/api/members/{id}/cancel", newMemberId).session(adminSession))
                .andExpect(status().isOk());
    }

    private LocalDateTime validStartTime() {
        LocalDate today = LocalDate.now();
        LocalDateTime candidate = LocalDateTime.now().plusMinutes(5).withSecond(0).withNano(0);
        LocalDateTime latest = today.atTime(LocalTime.of(22, 59));
        return candidate.isAfter(latest) ? today.atTime(23, 0) : candidate;
    }

    private MockHttpSession registerAndLogin(String phone) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType("application/json")
                        .content("{\"realName\":\"规则测试\",\"phone\":\"" + phone + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk());
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"" + phone + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }

    private MockHttpSession login(String username) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk()).andReturn().getRequest().getSession(false);
    }
}
