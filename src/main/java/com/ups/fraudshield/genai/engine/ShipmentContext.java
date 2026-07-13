package com.ups.fraudshield.genai.engine;

import java.time.LocalDateTime;
import java.util.List;

public record ShipmentContext(
        String bookingId,
        String shipperId,
        String originCountry,
        String destinationCountry,
        double weight,
        int packageCount,
        double declaredValue,
        String serviceType,
        String paymentMethod,
        LocalDateTime bookingTimestamp,
        String ipAddress,
        String deviceFingerprint,
        // Shipper historical profile data
        List<String> historicalDestinations,
        List<String> historicalOrigins,
        double averageWeight,
        double maxWeight,
        int averageShipmentsPerDay,
        List<String> knownPaymentMethods,
        List<Integer> usualBookingHours,
        List<String> usualIpRanges,
        List<String> usualDevices
) {}
