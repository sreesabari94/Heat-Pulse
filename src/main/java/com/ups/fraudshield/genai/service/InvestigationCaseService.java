package com.ups.fraudshield.genai.service;

import com.ups.fraudshield.genai.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InvestigationCaseService {

    private static final Logger log = LoggerFactory.getLogger(InvestigationCaseService.class);

    private final Map<String, InvestigationCase> caseStore = new ConcurrentHashMap<>();

    public InvestigationCaseService() {}

    public InvestigationCase createCase(RiskAssessment assessment) {
        String explanation = assessment.aiExplanation();
        Decision decision = Decision.valueOf(assessment.decision());

        InvestigationCase investigationCase = new InvestigationCase(
                UUID.randomUUID().toString(),
                assessment.bookingId(),
                assessment.riskScore(),
            decision,
            assessment.triggeredSignals(),
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
}
