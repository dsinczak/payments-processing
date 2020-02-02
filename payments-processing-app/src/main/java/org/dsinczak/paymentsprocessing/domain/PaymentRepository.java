package org.dsinczak.paymentsprocessing.domain;

import io.vavr.control.Option;

import java.util.UUID;

public interface PaymentRepository {

    UUID save(Payment payment);

    Option<Payment> findByPaymentId(UUID paymentId);

}
