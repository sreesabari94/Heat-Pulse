package com.ups.fraudshield.genai.controller;

import com.ups.fraudshield.genai.model.InvestigationCase;
import com.ups.fraudshield.genai.model.ReviewStatus;
import com.ups.fraudshield.genai.model.RiskAssessment;
import com.ups.fraudshield.genai.service.InvestigationCaseService;
import com.ups.fraudshield.genai.service.RiskAssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cases")
public class InvestigationCaseController {

    private final InvestigationCaseService caseService;
    private final RiskAssessmentService riskAssessmentService;

    public InvestigationCaseController(InvestigationCaseService caseService,
                                       RiskAssessmentService riskAssessmentService) {
        this.caseService = caseService;
        this.riskAssessmentService = riskAssessmentService;
    }

    @PostMapping("/create/{bookingId}")
    public ResponseEntity<InvestigationCase> createCase(@PathVariable String bookingId) {
        RiskAssessment assessment = riskAssessmentService.getByBookingId(bookingId);
        if (assessment == null) {
            return ResponseEntity.notFound().build();
        }
        InvestigationCase investigationCase = caseService.createCase(assessment);
        return ResponseEntity.ok(investigationCase);
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<InvestigationCase> getCase(@PathVariable String caseId) {
        InvestigationCase investigationCase = caseService.getCase(caseId);
        if (investigationCase == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(investigationCase);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<InvestigationCase> getByBookingId(@PathVariable String bookingId) {
        InvestigationCase investigationCase = caseService.getByBookingId(bookingId);
        if (investigationCase == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(investigationCase);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<InvestigationCase>> getCasesByStatus(@PathVariable ReviewStatus status) {
        return ResponseEntity.ok(caseService.getCasesByStatus(status));
    }

    @PatchMapping("/{caseId}/status")
    public ResponseEntity<InvestigationCase> updateStatus(@PathVariable String caseId,
                                                          @RequestBody UpdateStatusRequest request) {
        InvestigationCase updated = caseService.updateStatus(caseId, request.reviewStatus());
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    public record UpdateStatusRequest(ReviewStatus reviewStatus) {}
}
