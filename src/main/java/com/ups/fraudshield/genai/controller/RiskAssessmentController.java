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

        List<SignalResponse> signalResponses = assessment.signals().stream()
                .map(s -> new SignalResponse(s.signalCode(), s.signalName(), s.score()))
                .toList();

        String recommendedAction = switch (assessment.decision()) {
            case BLOCK -> "Block shipment immediately and escalate to fraud team.";
            case HOLD -> "Hold shipment and perform account validation.";
            case REVIEW -> "Review shipment details and verify shipper identity.";
            case ALLOW -> "Allow shipment to proceed with standard monitoring.";
        };

        EvaluationResponse response = new EvaluationResponse(
                assessment.assessmentId(),
                assessment.bookingId(),
                assessment.shipperId(),
                assessment.riskScore(),
                assessment.riskLevel().name(),
                assessment.decision().name(),
                assessment.confidence(),
                signalResponses,
                investigationCase.getGeminiExplanation(),
                recommendedAction
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
