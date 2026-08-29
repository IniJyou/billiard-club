package com.club.util;

import com.club.common.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public final class BillingCalculator {

    private BillingCalculator() {
    }

    /** 整小时进位：不足一小时按一小时，每超过一个完整小时进入下一小时。 */
    public static BillingAmounts calculate(LocalDateTime start, LocalDateTime end,
                                           BigDecimal hourlyPrice, BigDecimal discountRate) {
        if (start == null || end == null || hourlyPrice == null || discountRate == null) {
            throw new BusinessException("计费参数不完整");
        }
        long seconds = Math.max(1, Duration.between(start, end).getSeconds());
        long billedHours = (seconds + 3599) / 3600;
        BigDecimal durationHours = BigDecimal.valueOf(billedHours).setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal original = hourlyPrice.multiply(BigDecimal.valueOf(billedHours))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalAmount = original.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = original.subtract(finalAmount).setScale(2, RoundingMode.HALF_UP);
        int points = finalAmount.setScale(0, RoundingMode.DOWN).intValue();
        return new BillingAmounts(durationHours, original, discountRate, discountAmount, finalAmount, points);
    }

    public record BillingAmounts(BigDecimal durationHours, BigDecimal originalAmount,
                                 BigDecimal discountRate, BigDecimal discountAmount,
                                 BigDecimal finalAmount, int pointsEarned) {
    }
}
