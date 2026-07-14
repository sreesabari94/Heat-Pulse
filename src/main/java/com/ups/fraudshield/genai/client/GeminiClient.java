package com.ups.fraudshield.genai.client;

import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Hour 1: Vertex AI Gemini Java Client.
 * Spring Bean wrapping Gemini generative model calls.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final GenerativeModel generativeModel;

    public GeminiClient(GenerativeModel generativeModel) {
        this.generativeModel = generativeModel;
    }

    /**
     * Generates a business explanation for a fraud detection event.
     *
     * @param shipmentDetails  the shipment context
     * @param riskScore        the combined fraud score (0.0 - 1.0)
     * @param signals          list of triggered risk signals
     * @return business-language explanation from Gemini
     */
    public String generateExplanation(String shipmentDetails, double riskScore, List<String> signals) {
        String prompt = buildPrompt(shipmentDetails, riskScore, signals);
        return callGemini(prompt);
    }

    /**
     * Generates an investigator summary for human review.
     */
    public String generateInvestigatorSummary(String shipmentDetails, double riskScore, List<String> signals) {
        String prompt = buildInvestigatorPrompt(shipmentDetails, riskScore, signals);
        return callGemini(prompt);
    }

    private String callGemini(String prompt) {
        try {
            GenerateContentResponse response = generativeModel.generateContent(prompt);
            return ResponseHandler.getText(response);
        } catch (IOException e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            throw new GeminiCallException("Failed to generate explanation from Gemini", e);
        }
    }

    private String buildPrompt(String shipmentDetails, double riskScore, List<String> signals) {
        return """
                You are a UPS Fraud Analyst.
                Explain why this shipment was flagged.
                
                SHIPMENT DETAILS:
                %s
                
                RISK SCORE: %.2f (0-100)
                
                TRIGGERED SIGNALS:
                %s
                
                Provide your response in EXACTLY this format:
                
                RISK SUMMARY: [One-line summary of the risk level]
                
                FRAUD INDICATORS: [List key fraud indicators detected]
                
                AI EXPLANATION: [Shipment deviates significantly from normal behavior]
                
                RECOMMENDED ACTION: [Hold and verify account ownership or equivalent action]

                CONFIDENCE LEVEL: [HIGH/MEDIUM/LOW]
                """.formatted(shipmentDetails, riskScore, String.join("\n", signals));
    }

    private String buildInvestigatorPrompt(String shipmentDetails, double riskScore, List<String> signals) {
        return """
                You are a senior fraud investigator at UPS. Provide an investigator-grade summary for human review.
                
                SHIPMENT DETAILS:
                %s
                
                RISK SCORE: %.2f
                
                TRIGGERED SIGNALS:
                %s
                
                Provide your response in EXACTLY this format:
                
                EVIDENCE: [List the concrete evidence of potential fraud based on behavioral deviation]
                
                LIKELIHOOD: [Assess the probability this is genuine fraud vs. legitimate unusual activity]
                
                BUSINESS RECOMMENDATION: [Specific action steps - approve, hold for review, block, or escalate, with justification]
                """.formatted(shipmentDetails, riskScore, String.join("\n", signals));
    }

    public static class GeminiCallException extends RuntimeException {
        public GeminiCallException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
