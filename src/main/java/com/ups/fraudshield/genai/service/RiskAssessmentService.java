package com.ups.fraudshield.genai.service;

import com.ups.fraudshield.genai.client.GeminiClient;
import com.ups.fraudshield.genai.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    private final GeminiClient geminiClient;
    private final Map<String, RiskAssessment> assessmentStore = new ConcurrentHashMap<>();

    public RiskAssessmentService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public RiskAssessment evaluate(String bookingId, String shipperId, List<FraudSignal> signals) {
        int riskScore = calculateRiskScore(signals);
        String riskLevel = determineRiskLevel(riskScore);
        String decision = determineDecision(riskScore);

        RiskAssessment baseAssessment = new RiskAssessment(
                UUID.randomUUID().toString(),
                bookingId,
                shipperId,
                riskScore,
                riskLevel,
                decision,
                signals,
            null,
            null
        );

        AiResult aiResult = generateAiOutput(baseAssessment);
        String finalAiExplanation = aiResult.aiExplanation() == null || aiResult.aiExplanation().isBlank()
                ? buildFallbackExplanation(baseAssessment.riskLevel(), baseAssessment.triggeredSignals())
                : aiResult.aiExplanation();
        String finalRecommendedAction = aiResult.recommendedAction() == null || aiResult.recommendedAction().isBlank()
                ? defaultRecommendedAction(baseAssessment.decision())
                : aiResult.recommendedAction();

        RiskAssessment assessment = new RiskAssessment(
            baseAssessment.assessmentId(),
            baseAssessment.bookingId(),
            baseAssessment.shipperId(),
            baseAssessment.riskScore(),
            baseAssessment.riskLevel(),
            baseAssessment.decision(),
            baseAssessment.triggeredSignals(),
            finalAiExplanation,
            finalRecommendedAction
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

        public RiskAssessment generateAiFromAssessment(RiskAssessment inputAssessment) {
        List<FraudSignal> safeSignals = inputAssessment.triggeredSignals() == null
            ? List.of()
            : inputAssessment.triggeredSignals();
        int safeRiskScore = inputAssessment.riskScore() == null
            ? calculateRiskScore(safeSignals)
            : inputAssessment.riskScore();
        String safeRiskLevel = inputAssessment.riskLevel() == null || inputAssessment.riskLevel().isBlank()
            ? determineRiskLevel(safeRiskScore)
            : inputAssessment.riskLevel();
        String safeDecision = inputAssessment.decision() == null || inputAssessment.decision().isBlank()
            ? determineDecision(safeRiskScore)
            : inputAssessment.decision();

        RiskAssessment baseAssessment = new RiskAssessment(
            inputAssessment.assessmentId() == null || inputAssessment.assessmentId().isBlank()
                ? UUID.randomUUID().toString()
                : inputAssessment.assessmentId(),
            inputAssessment.bookingId(),
            inputAssessment.shipperId(),
            safeRiskScore,
            safeRiskLevel,
            safeDecision,
            safeSignals,
            null,
            null
        );

        AiResult aiResult = generateAiOutput(baseAssessment);
        String finalAiExplanation = aiResult.aiExplanation() == null || aiResult.aiExplanation().isBlank()
            ? buildFallbackExplanation(baseAssessment.riskLevel(), baseAssessment.triggeredSignals())
            : aiResult.aiExplanation();
        String finalRecommendedAction = aiResult.recommendedAction() == null || aiResult.recommendedAction().isBlank()
            ? defaultRecommendedAction(baseAssessment.decision())
            : aiResult.recommendedAction();

        RiskAssessment enrichedAssessment = new RiskAssessment(
            baseAssessment.assessmentId(),
            baseAssessment.bookingId(),
            baseAssessment.shipperId(),
            baseAssessment.riskScore(),
            baseAssessment.riskLevel(),
            baseAssessment.decision(),
            baseAssessment.triggeredSignals(),
            finalAiExplanation,
            finalRecommendedAction
        );

        assessmentStore.put(enrichedAssessment.assessmentId(), enrichedAssessment);
        return enrichedAssessment;
        }

    private int calculateRiskScore(List<FraudSignal> signals) {
        int totalScore = signals.stream()
                .mapToInt(FraudSignal::score)
                .sum();
        return Math.min(totalScore, 100);
    }

    private String determineRiskLevel(int riskScore) {
        if (riskScore <= 30) return "LOW";
        if (riskScore <= 70) return "MEDIUM";
        return "HIGH";
    }

    private String determineDecision(int riskScore) {
        if (riskScore <= 30) return "ALLOW";
        if (riskScore <= 70) return "REVIEW";
        return "BLOCK";
    }

        private AiResult generateAiOutput(RiskAssessment assessment) {
        List<String> signalDescriptions = assessment.triggeredSignals().stream()
                .map(s -> "- %s (%s): %s [score=%d]".formatted(
                        s.signalName(),
                        s.severity() != null ? s.severity() : "UNKNOWN",
                        s.reason() != null ? s.reason() : "No reason provided",
                        s.score()))
                .toList();

        String shipmentContext = "BookingId=%s, ShipperId=%s, RiskLevel=%s, Decision=%s"
            .formatted(
                assessment.bookingId(),
                assessment.shipperId(),
                assessment.riskLevel(),
                assessment.decision()
            );

        try {
            String response = geminiClient.generateExplanation(
                shipmentContext,
                assessment.riskScore(),
                signalDescriptions
            );
            String aiExplanation = extractSection(response, "AI EXPLANATION:");
            String recommendedAction = extractSection(response, "RECOMMENDED ACTION:");
            if (aiExplanation.equals("N/A")) {
                aiExplanation = response;
            }
            if (recommendedAction.equals("N/A")) {
            recommendedAction = defaultRecommendedAction(assessment.decision());
            }
            return new AiResult(aiExplanation.trim(), recommendedAction.trim());
        } catch (GeminiClient.GeminiCallException ex) {
            log.warn("Gemini failed for booking {}, using fallback explanation: {}",
                assessment.bookingId(), ex.getMessage());
            return new AiResult(
                buildFallbackExplanation(assessment.riskLevel(), assessment.triggeredSignals()),
                defaultRecommendedAction(assessment.decision())
            );
        } catch (RuntimeException ex) {
            log.warn("Gemini runtime failure for booking {}, using fallback explanation: {}",
                    assessment.bookingId(), ex.getMessage());
            return new AiResult(
                    buildFallbackExplanation(assessment.riskLevel(), assessment.triggeredSignals()),
                    defaultRecommendedAction(assessment.decision())
            );
        }
    }

    private String buildFallbackExplanation(String riskLevel, List<FraudSignal> signals) {
        String indicators = signals.stream()
                .map(s -> s.signalName() + "(" + s.score() + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("No indicators");
        return "RISK SUMMARY: %s risk shipment. FRAUD INDICATORS: %s.".formatted(riskLevel, indicators);
    }

    private String defaultRecommendedAction(String decision) {
        return switch (decision) {
            case "BLOCK" -> "Hold shipment and perform account validation.";
            case "REVIEW" -> "Route shipment for manual fraud review.";
            default -> "Allow shipment with standard monitoring.";
        };
    }

    private String extractSection(String response, String header) {
        int start = response.indexOf(header);
        if (start == -1) return "N/A";
        int valueStart = start + header.length();
        int nextLine = response.indexOf("\n", valueStart);
        if (nextLine == -1) {
            return response.substring(valueStart).trim();
        }
        return response.substring(valueStart, nextLine).trim();
    }

    private record AiResult(String aiExplanation, String recommendedAction) {
    }
}
