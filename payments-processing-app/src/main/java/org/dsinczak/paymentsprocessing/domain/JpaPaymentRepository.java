package org.dsinczak.paymentsprocessing.domain;

import io.vavr.control.Option;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.time.Clock;
import java.util.UUID;

public class JpaPaymentRepository implements PaymentRepository {

    private final EntityManager entityManager;
    private final Clock clock;

    public JpaPaymentRepository(EntityManager entityManager, Clock clock) {
        this.entityManager = entityManager;
        this.clock = clock;
    }

    @Override
    public UUID save(Payment payment) {
        entityManager.persist(payment);
        return payment.getPaymentId();
    }

    @Override
    public Option<Payment> findByPaymentId(UUID paymentId) {
        try {
            var payment = Option.of(entityManager.createQuery("SELECT p FROM Payment p WHERE p.paymentId = :paymentId", Payment.class)
                    .setParameter("paymentId", paymentId)
                    .setMaxResults(1)
                    .getSingleResult());

            // Inject dependencies
            payment.forEach(p -> p.setClock(clock));

            return payment;
        } catch (NoResultException e) {
            return Option.none();
        }
    }
}
