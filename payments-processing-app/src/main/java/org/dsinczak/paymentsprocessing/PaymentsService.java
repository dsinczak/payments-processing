package org.dsinczak.paymentsprocessing;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.dsinczak.paymentsprocessing.api.PaymentDto;
import org.dsinczak.paymentsprocessing.api.events.PaymentCancelledEvent;
import org.dsinczak.paymentsprocessing.api.events.PaymentCreatedEvent;
import org.dsinczak.paymentsprocessing.domain.CancellationFeePolicy;
import org.dsinczak.paymentsprocessing.domain.Payment;
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

/**
 * Domain Driven Design - Application Service
 * Service class orchestrates domain objects and implements business cases. It operates only on domain objects and
 * does not implement domain logic invariants.
 * Additionally it is service responsibility to add crosscutting concerns like transactions, logging etc.
 * More: <a href="https://blog.sapiensworks.com/post/2016/08/19/DDD-Application-Services-Explained">DDD Application Services Explained</a>
 */
@Slf4j
@Component
public class PaymentsService {

    private static final Set<Payment.Type> CREATION_NOTIFICATION_TYPES = HashSet.of(Payment.Type.TYPE1, Payment.Type.TYPE2);

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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Either<Seq<ErrorMessage>, UUID> createNewPayment(PaymentDto paymentDto) {
        log.info("Creating new payment {}", paymentDto);
        var payment = paymentFactory.create()
                .withType(paymentDto.getType())
                .withAmount(paymentDto.getAmount())
                .withCurrency(paymentDto.getCurrency())
                .withCreditor(paymentDto.getCreditorIban())
                .withCreditorBic(paymentDto.getCreditorBic())
                .withDebtor(paymentDto.getDebtorIban())
                .withDetails(paymentDto.getDetails())
                .build();

        // persist
        var paymentWithId = payment.map(p -> Tuple.of(paymentRepository.save(p), p));

        // send events (only for TYPE1 and TYPE2)
        paymentWithId.toOption()
                .filter(ip -> CREATION_NOTIFICATION_TYPES.contains(ip._2.getType()))
                .forEach(ip -> eventPublisher.publish(new PaymentCreatedEvent(ip._1.toString(), paymentDto.getType())));

        var result = paymentWithId.map(Tuple2::_1);
        log.debug("Payment creation result {}", result);

        return result;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Either<ErrorMessage, MonetaryAmount> cancelPayment(UUID paymentId) {
        log.info("Payment {} cancellation", paymentId);
        var cancellation = paymentRepository.findByPaymentId(paymentId)
                .toEither(error("Payment with ID: {0} does not exist.", paymentId))
                .flatMap(payment -> payment.cancel(cancellationFeePolicy).map(fee -> Tuple.of(fee, payment)));

        // Persist and send events
        cancellation.map(Tuple2::_2).forEach(payment -> {
            paymentRepository.save(payment);
            eventPublisher.publish(new PaymentCancelledEvent(paymentId.toString()));
        });

        var result = cancellation.map(Tuple2::_1);
        log.debug("Payment cancellation result {}", result);

        return result;
    }

}
