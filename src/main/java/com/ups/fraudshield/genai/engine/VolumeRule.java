package com.ups.fraudshield.genai.engine;

import com.ups.fraudshield.genai.model.FraudSignal;
import org.springframework.stereotype.Component;

@Component
public class VolumeRule implements Rule {

    @Override
    public String getRuleName() {
        return "VolumeRule";
    }

    @Override
    public RuleResult evaluate(ShipmentContext context) {
        if (context.averageShipmentsPerDay() <= 0) {
            return RuleResult.noMatch();
        }

        boolean isVolumeSpike = context.packageCount() > (5 * context.averageShipmentsPerDay());

        if (isVolumeSpike) {
            FraudSignal signal = new FraudSignal(
                    "VOL001",
                    SignalLibrary.VOLUME_SPIKE,
                    "HIGH",
                    30,
                    "Package count %d exceeds 5x average daily shipments %d".formatted(
                            context.packageCount(), context.averageShipmentsPerDay())
            );
            return RuleResult.match(signal);
        }

        return RuleResult.noMatch();
    }
}
