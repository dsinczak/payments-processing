package org.dsinczak.paymentsprocessing.domain;

import lombok.AllArgsConstructor;
import lombok.ToString;
import org.javamoney.moneta.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@ToString
public class Payment {

    enum Type {TYPE1, TYPE2, TYPE3}

    private UUID paymentId = UUID.randomUUID();
    private Iban debtor;
    private Iban creditor;
    private Bic creditorBic;
    private Money amount;
    private Instant created;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }
}
