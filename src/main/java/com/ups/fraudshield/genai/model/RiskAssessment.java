package com.ups.fraudshield.genai.model;

import java.util.List;

public record RiskAssessment(
        String assessmentId,
        String bookingId,
        String shipperId,
        Integer riskScore,
        String riskLevel,
        String decision,
        List<FraudSignal> triggeredSignals,
        String aiExplanation,
        String recommendedAction
) {}
