package org.dsinczak.paymentsprocessing.domain;

import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Cancellation fee is calculated as: h * k
 * <p>
 * Where:
 * h - number of full hours (2:59 = 2h) payment is in system;
 * k - coefficient (0.05 for TYPE1; 0.1 for TYPE2, 0.15 for TYPE3).
 * Result is an amount in EUR.
 */
public class ByHourCancellationFeePolicy implements CancellationFeePolicy {

    private final Clock clock;

    public ByHourCancellationFeePolicy(Clock clock) {
        this.clock = clock;
    }

    @Override
    public MonetaryAmount apply(LocalDateTime created, Payment.Type type, MonetaryAmount monetaryAmount) {
        var now = LocalDateTime.now(clock);
        if (now.isBefore(created)) {
            throw new IllegalArgumentException("Payment creation time cannot be after current time.");
        }
        var hours = Duration.between(created, now).toHours();

        return Money.of(hours, "EUR").multiply(coefficient(type));
    }

    private double coefficient(Payment.Type type) {
        switch (type) {
            case TYPE1:
                return 0.05;
            case TYPE2:
                return 0.1;
            case TYPE3:
                return 0.15;
            default:
                throw new IllegalArgumentException("Payment type: " + type + " is not supported.");
        }
    }
}
