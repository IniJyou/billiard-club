package com.club.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

public final class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private OrderNoGenerator() {
    }

    public static String next(String prefix) {
        int sequence = SEQUENCE.updateAndGet(value -> (value + 1) % 1000);
        return prefix + LocalDateTime.now().format(FORMATTER) + String.format("%03d", sequence);
    }
}
