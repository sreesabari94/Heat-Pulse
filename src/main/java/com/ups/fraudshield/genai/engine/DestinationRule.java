package com.ups.fraudshield.genai.engine;

import com.ups.fraudshield.genai.model.FraudSignal;
import org.springframework.stereotype.Component;

@Component
public class DestinationRule implements Rule {

    @Override
    public String getRuleName() {
        return "DestinationRule";
    }

    @Override
    public RuleResult evaluate(ShipmentContext context) {
        if (context.historicalDestinations() == null || context.historicalDestinations().isEmpty()) {
            return RuleResult.noMatch();
        }

        boolean isNewDestination = !context.historicalDestinations().contains(context.destinationCountry());

        if (isNewDestination) {
            FraudSignal signal = new FraudSignal(
                    "DEST001",
                    SignalLibrary.NEW_DESTINATION,
                    "HIGH",
                    25,
                    "Destination country '%s' not found in shipper's historical destinations".formatted(context.destinationCountry())
            );
            return RuleResult.match(signal);
        }

        return RuleResult.noMatch();
    }
}
