package org.dsinczak.paymentsprocessing;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashSet;
import io.vavr.collection.Seq;
import io.vavr.collection.Set;
import io.vavr.control.Either;
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
import static io.vavr.API.*;
import static io.vavr.Patterns.*;
import static io.vavr.Predicates.*;
import static org.dsinczak.paymentsprocessing.shared.ErrorMessage.error;


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

        // persist
        var paymentWithId = payment.map(p -> Tuple.of(paymentRepository.save(p), p));

        // send events (only for TYPE1 and TYPE2)
        paymentWithId.toOption()
                .filter(ip -> CREATION_NOTIFICATION_TYPES.contains(ip._2.getType()))
                .forEach(ip -> eventPublisher.publish(new PaymentCreatedEvent(ip._1, paymentDto.getType())));

        return paymentWithId.map(Tuple2::_1);
    }

    @Transactional
    public Either<ErrorMessage, MonetaryAmount> cancelPayment(UUID paymentId) {
        var cancellation = paymentRepository.findByPaymentId(paymentId)
                .toEither(error("Payment with ID: {} does not exist.", paymentId))
                .flatMap(payment -> payment.cancel(cancellationFeePolicy).map(fee -> Tuple.of(fee, payment)));

        // Persist and send events
        cancellation.map(Tuple2::_2).forEach(payment -> {
            paymentRepository.save(payment);
            eventPublisher.publish(new PaymentCancelledEvent(paymentId));
        });

        return cancellation.map(Tuple2::_1);
    }

}
