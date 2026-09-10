package com.club;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberCIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void upgradesLegacyMd5ToBcryptAndCanLoginAgain() throws Exception {
        login("admin");
        String upgraded = jdbcTemplate.queryForObject(
                "SELECT password FROM sys_user WHERE username = 'admin'", String.class);
        assertThat(upgraded).startsWith("$2").hasSize(60);
        login("admin");
    }

    @Test
    void reportIsAdminOnlyAndValidatesDateRange() throws Exception {
        mockMvc.perform(get("/api/reports/overview").session(login("cashier")))
                .andExpect(status().isForbidden());
        MockHttpSession admin = login("admin");
        mockMvc.perform(get("/api/reports/overview").session(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.startDate").value(LocalDate.now().withDayOfMonth(1).toString()));
        mockMvc.perform(get("/api/reports/overview").session(admin)
                        .param("startDate", "2026-02-02").param("endDate", "2026-02-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculatesReportMetricsFromBillsRechargesAndSessions() throws Exception {
        LocalDateTime reportTime = LocalDateTime.now().withSecond(0).withNano(0);
        LocalDateTime sessionStart = reportTime.minusMinutes(60);
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        if (sessionStart.isBefore(dayStart)) {
            sessionStart = dayStart;
        }
        long expectedUsedMinutes = Duration.between(sessionStart, reportTime).toMinutes();
        jdbcTemplate.update("INSERT INTO member(id, card_no, name, phone, level_id, balance, points, status) " +
                "VALUES (900, 'V900', '报表会员', '13990000900', 1, 0, 0, 1)");
        jdbcTemplate.update("INSERT INTO table_session(id, session_no, table_id, member_id, start_time, end_time, status, operator_id) " +
                "VALUES (900, 'S900', 1, 900, ?, ?, 1, 1)", sessionStart, reportTime);
        jdbcTemplate.update("INSERT INTO order_bill(id, bill_no, session_id, member_id, duration_hours, original_amount, " +
                        "discount_rate, discount_amount, final_amount, pay_way, points_earned, operator_id, create_time) " +
                        "VALUES (900, 'B900', 900, 900, 1, 35, 0.85, 5, 30, 1, 30, 1, ?)", reportTime);
        jdbcTemplate.update("INSERT INTO recharge_record(id, record_no, member_id, amount, gift_amount, pay_way, operator_id, create_time) " +
                "VALUES (900, 'R900', 900, 100, 10, 4, 1, ?)", reportTime);

        String today = LocalDate.now().toString();
        mockMvc.perform(get("/api/reports/overview").session(login("admin"))
                        .param("startDate", today).param("endDate", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summary.consumptionRevenue").value(30.0))
                .andExpect(jsonPath("$.data.summary.rechargePrincipal").value(100.0))
                .andExpect(jsonPath("$.data.summary.discountAmount").value(5.0))
                .andExpect(jsonPath("$.data.summary.memberOrderCount").value(1))
                .andExpect(jsonPath("$.data.dailyTrend[0].orderCount").value(1))
                .andExpect(jsonPath("$.data.tableUsage[0].usedMinutes").value(expectedUsedMinutes));
    }

    @Test
    void filtersRecordsAndExportsExcelFriendlyCsv() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("INSERT INTO member(id, card_no, name, phone, level_id, balance, points, status) " +
                "VALUES (901, 'V901', '筛选会员', '13990000901', 1, 0, 0, 1)");
        jdbcTemplate.update("INSERT INTO recharge_record(id, record_no, member_id, amount, gift_amount, pay_way, operator_id, create_time) " +
                "VALUES (901, 'R901', 901, 88, 0, 4, 2, ?)", now);
        MockHttpSession session = login("cashier");
        String today = LocalDate.now().toString();

        mockMvc.perform(get("/api/records/recharges").session(session)
                        .param("keyword", "筛选会员").param("startDate", today).param("endDate", today)
                        .param("payWay", "4").param("operatorId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].recordNo").value("R901"));

        byte[] csv = mockMvc.perform(get("/api/records/recharges/export").session(session)
                        .param("payWay", "4").param("operatorId", "2"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertThat(csv).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
        assertThat(new String(csv, 3, csv.length - 3, StandardCharsets.UTF_8))
                .contains("充值单号", "R901", "筛选会员", "银行卡");

        mockMvc.perform(get("/api/records/operators").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(get("/api/records/recharges").session(session).param("startDate", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("查询参数格式不正确"));
    }

    private MockHttpSession login(String username) throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }
}
