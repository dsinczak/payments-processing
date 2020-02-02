package org.dsinczak.paymentsprocessing.domain;

import io.vavr.Function3;
import org.javamoney.moneta.Money;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;

/**
 * Cancellation fee policy allows us to maintain open-closed principle when
 * counting fee. So when times comes to have new approach, we do not have to
 * modify domain logic but only providing new implementation that keeps the
 * contract.
 */
public interface CancellationFeePolicy extends Function3<LocalDateTime, Payment.Type, MonetaryAmount, MonetaryAmount> {

}
