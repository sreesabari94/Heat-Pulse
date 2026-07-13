package com.ups.fraudshield.genai.model;

import java.util.List;

public class InvestigationCase {

    private String caseId;
    private String bookingId;
    private int riskScore;
    private Decision decision;
    private List<FraudSignal> signals;
    private String geminiExplanation;
    private ReviewStatus reviewStatus;

    public InvestigationCase() {}

    public InvestigationCase(String caseId, String bookingId, int riskScore, Decision decision,
                             List<FraudSignal> signals, String geminiExplanation, ReviewStatus reviewStatus) {
        this.caseId = caseId;
        this.bookingId = bookingId;
        this.riskScore = riskScore;
        this.decision = decision;
        this.signals = signals;
        this.geminiExplanation = geminiExplanation;
        this.reviewStatus = reviewStatus;
    }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public List<FraudSignal> getSignals() { return signals; }
    public void setSignals(List<FraudSignal> signals) { this.signals = signals; }

    public String getGeminiExplanation() { return geminiExplanation; }
    public void setGeminiExplanation(String geminiExplanation) { this.geminiExplanation = geminiExplanation; }

    public ReviewStatus getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(ReviewStatus reviewStatus) { this.reviewStatus = reviewStatus; }
}
