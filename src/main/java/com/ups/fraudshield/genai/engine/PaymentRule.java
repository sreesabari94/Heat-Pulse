package com.ups.fraudshield.genai.engine;

import com.ups.fraudshield.genai.model.FraudSignal;
import org.springframework.stereotype.Component;

@Component
public class PaymentRule implements Rule {

    @Override
    public String getRuleName() {
        return "PaymentRule";
    }

    @Override
    public RuleResult evaluate(ShipmentContext context) {
        if (context.knownPaymentMethods() == null || context.knownPaymentMethods().isEmpty()) {
            return RuleResult.noMatch();
        }

        boolean isNewPayment = !context.knownPaymentMethods().contains(context.paymentMethod());

        if (isNewPayment) {
            FraudSignal signal = new FraudSignal(
                    "PAY001",
                    SignalLibrary.NEW_PAYMENT_METHOD,
                    "MEDIUM",
                    20,
                    "Payment method '%s' not in known methods: %s".formatted(
                            context.paymentMethod(), context.knownPaymentMethods())
            );
            return RuleResult.match(signal);
        }

        return RuleResult.noMatch();
    }
}
