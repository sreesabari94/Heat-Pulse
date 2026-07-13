package com.ups.fraudshield.genai.engine;

import com.ups.fraudshield.genai.model.FraudSignal;
import org.springframework.stereotype.Component;

@Component
public class BookingTimeRule implements Rule {

    @Override
    public String getRuleName() {
        return "BookingTimeRule";
    }

    @Override
    public RuleResult evaluate(ShipmentContext context) {
        if (context.usualBookingHours() == null || context.usualBookingHours().isEmpty()) {
            return RuleResult.noMatch();
        }

        int bookingHour = context.bookingTimestamp().getHour();
        boolean isOddHour = !context.usualBookingHours().contains(bookingHour);

        if (isOddHour) {
            FraudSignal signal = new FraudSignal(
                    "TM001",
                    SignalLibrary.ODD_BOOKING_HOUR,
                    "LOW",
                    10,
                    "Booking at hour %d is outside usual booking hours %s".formatted(
                            bookingHour, context.usualBookingHours())
            );
            return RuleResult.match(signal);
        }

        return RuleResult.noMatch();
    }
}
