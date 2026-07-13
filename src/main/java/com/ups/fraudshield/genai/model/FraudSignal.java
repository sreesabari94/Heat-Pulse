package com.ups.fraudshield.genai.model;

public record FraudSignal(
        String signalCode,
        String signalName,
        String severity,
        int score,
        String reason
) {}
