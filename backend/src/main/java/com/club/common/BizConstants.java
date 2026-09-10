package com.club.common;

public final class BizConstants {

    public static final int ENABLED = 1;
    public static final int DISABLED = 0;
    public static final int MEMBER_CANCELLED = 2;

    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_CASHIER = 2;
    public static final int ROLE_USER = 3;

    public static final int TABLE_IDLE = 0;
    public static final int TABLE_IN_USE = 1;
    public static final int TABLE_MAINTENANCE = 2;

    public static final int SESSION_ACTIVE = 0;
    public static final int SESSION_CHECKED_OUT = 1;
    public static final int SESSION_CANCELLED = 2;

    public static final int PAY_CASH = 1;
    public static final int PAY_BALANCE = 2;
    public static final int PAY_CREDIT = 3;

    public static final int RESERVATION_PENDING = 0;
    public static final int RESERVATION_OPENED = 1;
    public static final int RESERVATION_COMPLETED = 2;
    public static final int RESERVATION_CANCELLED = 3;

    private BizConstants() {
    }
}
