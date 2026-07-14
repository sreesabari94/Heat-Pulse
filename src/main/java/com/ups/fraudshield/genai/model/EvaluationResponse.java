package com.ups.fraudshield.genai.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EvaluationResponse(
        @JsonProperty("AssessmentId")
        String assessmentId,
        String bookingId,
        @JsonProperty("ShipperId")
        String shipperId,
        int riskScore,
        String riskLevel,
        String decision,
        List<SignalResponse> signals,
        String aiExplanation,
        String recommendedAction
) {}
