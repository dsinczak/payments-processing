package org.dsinczak.paymentsprocessing.domain;

import io.vavr.control.Option;

import java.util.UUID;

// TODO
public class PaymentRepository {

    public UUID save(Payment payment) {
        return UUID.randomUUID();
    }

    public Option<Payment> findByPaymentId(UUID paymentId) {
        return Option.none();
    }

}
