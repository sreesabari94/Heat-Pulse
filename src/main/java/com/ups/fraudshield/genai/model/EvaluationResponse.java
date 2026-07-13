package com.ups.fraudshield.genai.model;

import java.util.List;

public record EvaluationResponse(
        String assessmentId,
        String bookingId,
        String shipperId,
        int riskScore,
        String riskLevel,
        String decision,
        double confidence,
        List<SignalResponse> signals,
        String explanation,
        String recommendedAction
) {}
