package com.reservation.hotel.constants;


public final class AppConstants {

    private AppConstants() {
        throw new IllegalStateException("Utility class - do not instantiate");
    }

    // Trace ID constants
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String TRACE_ID_MDC_KEY = "traceId";

    // Transaction format constants
    public static final int E2E_ID_LENGTH = 10;
    public static final int RESERVATION_ID_LENGTH = 8;
    public static final int EXPECTED_TRANSACTION_PARTS = 2;
}

