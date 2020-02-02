package org.dsinczak.paymentsprocessing;

import io.vavr.collection.Seq;
import io.vavr.control.Either;
import org.dsinczak.paymentsprocessing.api.PaymentDto;
import org.dsinczak.paymentsprocessing.api.events.PaymentCancelledEvent;
import org.dsinczak.paymentsprocessing.api.events.PaymentCreatedEvent;
import org.dsinczak.paymentsprocessing.domain.CancellationFeePolicy;
import org.dsinczak.paymentsprocessing.domain.PaymentFactory;
import org.dsinczak.paymentsprocessing.domain.PaymentRepository;
import org.dsinczak.paymentsprocessing.notification.EventPublisher;
import org.dsinczak.paymentsprocessing.shared.ErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.money.MonetaryAmount;
import javax.transaction.Transactional;
import java.util.UUID;

import static org.dsinczak.paymentsprocessing.shared.ErrorMessage.error;

@Component
public class PaymentsService {

    private final PaymentFactory paymentFactory;
    private final PaymentRepository paymentRepository;
    private final CancellationFeePolicy cancellationFeePolicy;
    private final EventPublisher eventPublisher;

    @Autowired
    public PaymentsService(PaymentFactory paymentFactory, PaymentRepository paymentRepository, CancellationFeePolicy cancellationFeePolicy, EventPublisher eventPublisher) {
        this.paymentFactory = paymentFactory;
        this.paymentRepository = paymentRepository;
        this.cancellationFeePolicy = cancellationFeePolicy;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Either<Seq<ErrorMessage>, UUID> createNewPayment(PaymentDto paymentDto) {
        var payment = paymentFactory.create()
                .withType(paymentDto.getType())
                .withAmount(paymentDto.getAmount())
                .withCurrency(paymentDto.getCurrency())
                .withCreditor(paymentDto.getCreditorIban())
                .withCreditorBic(paymentDto.getCreditorBic())
                .withDebtor(paymentDto.getDebtorIban())
                .withDetails(paymentDto.getDetails())
                .build();

        var paymentId = payment.map(paymentRepository::save);

        paymentId.forEach(id -> eventPublisher.publish(new PaymentCreatedEvent(id, paymentDto.getType())));

        return paymentId;
    }

    @Transactional
    public Either<ErrorMessage, MonetaryAmount> cancelPayment(UUID paymentId) {
        var cancellation = paymentRepository.findByPaymentId(paymentId)
                .toEither(() -> error("Payment with ID: {} does not exist.", paymentId))
                .flatMap(payment -> payment.cancel(cancellationFeePolicy));

        cancellation.forEach(c -> eventPublisher.publish(new PaymentCancelledEvent(paymentId)));

        return cancellation;
    }

}
