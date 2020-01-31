package org.dsinczak.paymentsprocessing.domain;

import org.javamoney.moneta.Money;

import java.time.Instant;

public interface CancellationFeePolicy {

    Money calculate(Instant created, Payment.Type type, Money amount);

}
