package com.ups.fraudshield.genai.model;

import java.time.LocalDateTime;
import java.util.List;

public record RiskAssessment(
        String assessmentId,
        String bookingId,
        String shipperId,
        int riskScore,
        RiskLevel riskLevel,
        Decision decision,
        double confidence,
        List<FraudSignal> signals,
        LocalDateTime assessmentTime
) {}
