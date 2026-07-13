package com.ups.fraudshield.genai.service;

import com.ups.fraudshield.genai.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    private final Map<String, RiskAssessment> assessmentStore = new ConcurrentHashMap<>();

    public RiskAssessment evaluate(String bookingId, String shipperId, List<FraudSignal> signals) {
        int riskScore = calculateRiskScore(signals);
        RiskLevel riskLevel = determineRiskLevel(riskScore);
        Decision decision = determineDecision(riskScore, riskLevel);
        double confidence = calculateConfidence(signals, riskScore);

        RiskAssessment assessment = new RiskAssessment(
                UUID.randomUUID().toString(),
                bookingId,
                shipperId,
                riskScore,
                riskLevel,
                decision,
                confidence,
                signals,
                LocalDateTime.now()
        );

        assessmentStore.put(assessment.assessmentId(), assessment);
        log.info("Risk assessment completed for booking {}: score={}, level={}, decision={}",
                bookingId, riskScore, riskLevel, decision);

        return assessment;
    }

    public RiskAssessment getAssessment(String assessmentId) {
        return assessmentStore.get(assessmentId);
    }

    public RiskAssessment getByBookingId(String bookingId) {
        return assessmentStore.values().stream()
                .filter(a -> a.bookingId().equals(bookingId))
                .findFirst()
                .orElse(null);
    }

    private int calculateRiskScore(List<FraudSignal> signals) {
        int totalScore = signals.stream()
                .mapToInt(FraudSignal::score)
                .sum();
        return Math.min(totalScore, 100);
    }

    private RiskLevel determineRiskLevel(int riskScore) {
        if (riskScore <= 30) return RiskLevel.LOW;
        if (riskScore <= 70) return RiskLevel.MEDIUM;
        return RiskLevel.HIGH;
    }

    private Decision determineDecision(int riskScore, RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> Decision.ALLOW;
            case MEDIUM -> Decision.REVIEW;
            case HIGH -> riskScore >= 85 ? Decision.BLOCK : Decision.HOLD;
        };
    }

    private double calculateConfidence(List<FraudSignal> signals, int riskScore) {
        if (signals.isEmpty()) return 0.5;

        long highSeverityCount = signals.stream()
                .filter(s -> "HIGH".equalsIgnoreCase(s.severity()))
                .count();

        double base = 0.6;
        double signalBoost = Math.min(signals.size() * 0.05, 0.2);
        double severityBoost = Math.min(highSeverityCount * 0.05, 0.15);
        double scoreBoost = (riskScore > 70 || riskScore < 20) ? 0.05 : 0.0;

        return Math.min(base + signalBoost + severityBoost + scoreBoost, 1.0);
    }
}
