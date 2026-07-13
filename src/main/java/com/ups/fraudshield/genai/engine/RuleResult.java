package com.ups.fraudshield.genai.engine;

import com.ups.fraudshield.genai.model.FraudSignal;

public record RuleResult(
        boolean triggered,
        FraudSignal signal
) {
    public static RuleResult noMatch() {
        return new RuleResult(false, null);
    }

    public static RuleResult match(FraudSignal signal) {
        return new RuleResult(true, signal);
    }
}
