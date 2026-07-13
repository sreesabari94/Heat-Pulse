package com.ups.fraudshield.genai.model;

public record SignalResponse(
        String code,
        String name,
        int score
) {}
