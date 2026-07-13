package com.ups.fraudshield.genai.engine;

public final class SignalLibrary {

    private SignalLibrary() {}

    // Destination Signals
    public static final String NEW_DESTINATION = "NEW_DESTINATION";
    public static final String HIGH_RISK_COUNTRY = "HIGH_RISK_COUNTRY";
    public static final String UNUSUAL_ROUTE = "UNUSUAL_ROUTE";

    // Weight Signals
    public static final String WEIGHT_SPIKE = "WEIGHT_SPIKE";
    public static final String HEAVY_PACKAGE = "HEAVY_PACKAGE";

    // Volume Signals
    public static final String VOLUME_SPIKE = "VOLUME_SPIKE";
    public static final String BULK_BOOKING = "BULK_BOOKING";

    // Account Signals
    public static final String NEW_DEVICE = "NEW_DEVICE";
    public static final String NEW_IP = "NEW_IP";
    public static final String NEW_PAYMENT_METHOD = "NEW_PAYMENT_METHOD";

    // Timing Signals
    public static final String ODD_BOOKING_HOUR = "ODD_BOOKING_HOUR";
    public static final String HOLIDAY_ACTIVITY = "HOLIDAY_ACTIVITY";
}
