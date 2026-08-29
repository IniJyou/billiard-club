package com.club.common;

public final class NumberUtils {

    private NumberUtils() {
    }

    public static long positivePage(Long value, long defaultValue) {
        return value == null || value < 1 ? defaultValue : value;
    }

    public static long pageSize(Long value) {
        if (value == null || value < 1) {
            return 10;
        }
        return Math.min(value, 100);
    }
}
