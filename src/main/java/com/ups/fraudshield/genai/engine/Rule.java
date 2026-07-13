package com.ups.fraudshield.genai.engine;

public interface Rule {

    String getRuleName();

    RuleResult evaluate(ShipmentContext context);
}
