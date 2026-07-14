package com.ups.fraudshield.genai.controller;

import com.ups.fraudshield.genai.model.*;
import com.ups.fraudshield.genai.service.InvestigationCaseService;
import com.ups.fraudshield.genai.service.RiskAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskAssessmentController {

    private final RiskAssessmentService riskAssessmentService;
    private final InvestigationCaseService caseService;

    public RiskAssessmentController(RiskAssessmentService riskAssessmentService,
                                    InvestigationCaseService caseService) {
        this.riskAssessmentService = riskAssessmentService;
        this.caseService = caseService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationResponse> evaluate(@RequestBody EvaluateRequest request) {
        RiskAssessment assessment = riskAssessmentService.evaluate(request.bookingId(), request.shipperId(), request.signals());
        InvestigationCase investigationCase = caseService.createCase(assessment);

        List<SignalResponse> signalResponses = assessment.triggeredSignals().stream()
                .map(s -> new SignalResponse(s.signalCode(), s.signalName(), s.score()))
                .toList();

        EvaluationResponse response = new EvaluationResponse(
                assessment.assessmentId(),
                assessment.bookingId(),
                assessment.shipperId(),
                assessment.riskScore(),
            assessment.riskLevel(),
            assessment.decision(),
                signalResponses,
            assessment.aiExplanation(),
            assessment.recommendedAction()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-ai")
    public ResponseEntity<EvaluationResponse> generateAi(@RequestBody RiskAssessment request) {
        RiskAssessment enrichedAssessment = riskAssessmentService.generateAiFromAssessment(request);

        List<SignalResponse> signalResponses = enrichedAssessment.triggeredSignals().stream()
                .map(s -> new SignalResponse(s.signalCode(), s.signalName(), s.score()))
                .toList();

        EvaluationResponse response = new EvaluationResponse(
                enrichedAssessment.assessmentId(),
                enrichedAssessment.bookingId(),
                enrichedAssessment.shipperId(),
                enrichedAssessment.riskScore(),
                enrichedAssessment.riskLevel(),
                enrichedAssessment.decision(),
                signalResponses,
                enrichedAssessment.aiExplanation(),
                enrichedAssessment.recommendedAction()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assessment/{assessmentId}")
    public ResponseEntity<RiskAssessment> getAssessment(@PathVariable String assessmentId) {
        RiskAssessment assessment = riskAssessmentService.getAssessment(assessmentId);
        if (assessment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(assessment);
    }

    @GetMapping("/assessment/booking/{bookingId}")
    public ResponseEntity<RiskAssessment> getByBookingId(@PathVariable String bookingId) {
        RiskAssessment assessment = riskAssessmentService.getByBookingId(bookingId);
        if (assessment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(assessment);
    }

    public record EvaluateRequest(
            String bookingId,
            String shipperId,
            List<FraudSignal> signals
    ) {}
}
