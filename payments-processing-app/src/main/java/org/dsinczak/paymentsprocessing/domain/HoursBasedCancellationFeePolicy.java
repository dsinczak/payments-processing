package org.dsinczak.paymentsprocessing.domain;

import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cancellation fee is calculated as: h * k
 *
 * Where:
 *  h - number of full hours (2:59 = 2h) payment is in system;
 *  k - coefficient (0.05 for TYPE1; 0.1 for TYPE2, 0.15 for TYPE3).
 * Result is an amount in EUR.
 */
public class HoursBasedCancellationFeePolicy implements CancellationFeePolicy {
    @Override
    public MonetaryAmount apply(LocalDateTime created, Payment.Type type, MonetaryAmount monetaryAmount) {
        return Money.of(BigDecimal.ONE, "EUR");
    }
}
