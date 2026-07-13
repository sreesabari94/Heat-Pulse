package com.ups.fraudshield.genai.engine;

import com.ups.fraudshield.genai.model.Decision;
import com.ups.fraudshield.genai.model.FraudSignal;
import com.ups.fraudshield.genai.model.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiskCalculator {

    private static final Logger log = LoggerFactory.getLogger(RiskCalculator.class);

    private final List<Rule> rules;

    public RiskCalculator(List<Rule> rules) {
        this.rules = rules;
    }

    public RiskCalculationResult calculate(ShipmentContext context) {
        List<FraudSignal> triggeredSignals = rules.stream()
                .map(rule -> rule.evaluate(context))
                .filter(RuleResult::triggered)
                .map(RuleResult::signal)
                .toList();

        int totalScore = triggeredSignals.stream()
                .mapToInt(FraudSignal::score)
                .sum();
        totalScore = Math.min(totalScore, 100);

        RiskLevel riskLevel = determineRiskLevel(totalScore);
        Decision decision = determineDecision(totalScore);

        log.info("Risk calculation for booking {}: score={}, level={}, decision={}, signals={}",
                context.bookingId(), totalScore, riskLevel, decision, triggeredSignals.size());

        return new RiskCalculationResult(totalScore, riskLevel, decision, triggeredSignals);
    }

    private RiskLevel determineRiskLevel(int score) {
        if (score <= 30) return RiskLevel.LOW;
        if (score <= 70) return RiskLevel.MEDIUM;
        return RiskLevel.HIGH;
    }

    private Decision determineDecision(int score) {
        if (score <= 30) return Decision.ALLOW;
        if (score <= 70) return Decision.REVIEW;
        return Decision.BLOCK;
    }

    public record RiskCalculationResult(
            int totalScore,
            RiskLevel riskLevel,
            Decision decision,
            List<FraudSignal> signals
    ) {}
}
