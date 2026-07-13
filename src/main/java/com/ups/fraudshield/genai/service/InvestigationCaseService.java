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
import java.util.stream.Collectors;

@Service
public class InvestigationCaseService {

    private static final Logger log = LoggerFactory.getLogger(InvestigationCaseService.class);

    private final GeminiClient geminiClient;
    private final Map<String, InvestigationCase> caseStore = new ConcurrentHashMap<>();

    public InvestigationCaseService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public InvestigationCase createCase(RiskAssessment assessment) {
        String explanation = generateExplanation(assessment);

        InvestigationCase investigationCase = new InvestigationCase(
                UUID.randomUUID().toString(),
                assessment.bookingId(),
                assessment.riskScore(),
                assessment.decision(),
                assessment.signals(),
                explanation,
                ReviewStatus.OPEN
        );

        caseStore.put(investigationCase.getCaseId(), investigationCase);
        log.info("Investigation case {} created for booking {}", investigationCase.getCaseId(), assessment.bookingId());

        return investigationCase;
    }

    public InvestigationCase getCase(String caseId) {
        return caseStore.get(caseId);
    }

    public InvestigationCase getByBookingId(String bookingId) {
        return caseStore.values().stream()
                .filter(c -> c.getBookingId().equals(bookingId))
                .findFirst()
                .orElse(null);
    }

    public List<InvestigationCase> getCasesByStatus(ReviewStatus status) {
        return caseStore.values().stream()
                .filter(c -> c.getReviewStatus() == status)
                .toList();
    }

    public InvestigationCase updateStatus(String caseId, ReviewStatus newStatus) {
        InvestigationCase investigationCase = caseStore.get(caseId);
        if (investigationCase == null) {
            return null;
        }
        investigationCase.setReviewStatus(newStatus);
        log.info("Case {} status updated to {}", caseId, newStatus);
        return investigationCase;
    }

    private String generateExplanation(RiskAssessment assessment) {
        List<String> signalDescriptions = assessment.signals().stream()
                .map(s -> "- %s (%s): %s [score: %d]".formatted(s.signalName(), s.severity(), s.reason(), s.score()))
                .toList();

        try {
            String context = "Booking: %s, Risk Score: %d, Decision: %s".formatted(
                    assessment.bookingId(), assessment.riskScore(), assessment.decision());
            return geminiClient.generateExplanation(context, assessment.riskScore() / 100.0, signalDescriptions);
        } catch (GeminiClient.GeminiCallException e) {
            log.warn("Gemini call failed for case explanation, using fallback: {}", e.getMessage());
            return buildFallbackExplanation(assessment);
        }
    }

    private String buildFallbackExplanation(RiskAssessment assessment) {
        String signalSummary = assessment.signals().stream()
                .map(s -> s.signalName() + ": " + s.reason())
                .collect(Collectors.joining("; "));

        return "Risk score %d (%s). Decision: %s. Signals: %s".formatted(
                assessment.riskScore(),
                assessment.riskLevel(),
                assessment.decision(),
                signalSummary
        );
    }
}
