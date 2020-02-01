package org.dsinczak.paymentsprocessing.domain;

import io.vavr.Function3;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;

public interface CancellationFeePolicy extends Function3<LocalDateTime, Payment.Type, MonetaryAmount, MonetaryAmount> {

}
