package com.club.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BillingCalculatorTest {

    private final LocalDateTime start = LocalDateTime.of(2026, 8, 29, 12, 0);

    @Test
    void roundsOneMinuteUpToOneHour() {
        var result = BillingCalculator.calculate(start, start.plusMinutes(1),
                new BigDecimal("20.00"), BigDecimal.ONE);
        assertThat(result.durationHours()).isEqualByComparingTo("1.00");
        assertThat(result.finalAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void keepsExactlySixtyMinutesAtOneHour() {
        var result = BillingCalculator.calculate(start, start.plusMinutes(60),
                new BigDecimal("20.00"), BigDecimal.ONE);
        assertThat(result.durationHours()).isEqualByComparingTo("1.00");
    }

    @Test
    void roundsSixtyOneMinutesUpToTwoHoursAndAppliesDiscount() {
        var result = BillingCalculator.calculate(start, start.plusMinutes(61),
                new BigDecimal("20.00"), new BigDecimal("0.95"));
        assertThat(result.durationHours()).isEqualByComparingTo("2.00");
        assertThat(result.originalAmount()).isEqualByComparingTo("40.00");
        assertThat(result.finalAmount()).isEqualByComparingTo("38.00");
        assertThat(result.pointsEarned()).isEqualTo(38);
    }
}
