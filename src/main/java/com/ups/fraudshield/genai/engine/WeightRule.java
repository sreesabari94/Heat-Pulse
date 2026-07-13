package com.ups.fraudshield.genai.engine;

import com.ups.fraudshield.genai.model.FraudSignal;
import org.springframework.stereotype.Component;

@Component
public class WeightRule implements Rule {

    @Override
    public String getRuleName() {
        return "WeightRule";
    }

    @Override
    public RuleResult evaluate(ShipmentContext context) {
        if (context.averageWeight() <= 0) {
            return RuleResult.noMatch();
        }

        boolean isWeightSpike = context.weight() > (3 * context.averageWeight());

        if (isWeightSpike) {
            FraudSignal signal = new FraudSignal(
                    "WT001",
                    SignalLibrary.WEIGHT_SPIKE,
                    "HIGH",
                    20,
                    "Shipment weight %.1f kg exceeds 3x average historical weight %.1f kg".formatted(
                            context.weight(), context.averageWeight())
            );
            return RuleResult.match(signal);
        }

        return RuleResult.noMatch();
    }
}
