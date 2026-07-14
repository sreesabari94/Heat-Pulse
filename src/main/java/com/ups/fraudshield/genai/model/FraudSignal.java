package com.ups.fraudshield.genai.model;

public record FraudSignal(
        String signalCode,
        String signalName,
        String severity,
        Integer score,
        String reason
) {}
