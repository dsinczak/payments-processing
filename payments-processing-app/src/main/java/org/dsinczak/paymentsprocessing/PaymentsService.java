package org.dsinczak.paymentsprocessing;

import org.dsinczak.paymentsprocessing.domain.CancellationFeePolicy;
import org.dsinczak.paymentsprocessing.domain.PaymentFactory;
import org.dsinczak.paymentsprocessing.domain.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentsService {

    private final PaymentFactory paymentFactory;
    private final PaymentRepository paymentRepository;
    private final CancellationFeePolicy cancellationFeePolicy;

    @Autowired
    public PaymentsService(PaymentFactory paymentFactory, PaymentRepository paymentRepository, CancellationFeePolicy cancellationFeePolicy) {
        this.paymentFactory = paymentFactory;
        this.paymentRepository = paymentRepository;
        this.cancellationFeePolicy = cancellationFeePolicy;
    }

}
