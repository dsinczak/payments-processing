package org.dsinczak.paymentsprocessing.domain;

import io.vavr.control.Either;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dsinczak.paymentsprocessing.shared.ErrorMessage;
import org.dsinczak.paymentsprocessing.shared.MonetaryAmountConverter;

import javax.money.MonetaryAmount;
import javax.persistence.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

import static org.dsinczak.paymentsprocessing.shared.ErrorMessage.error;

/**
 * DDD Aggregate root for payment.
 * More: https://medium.com/withbetterco/using-aggregates-and-factories-in-domain-driven-design-34e0dff220c3
 * Encapsulates business rules for purchase providing coherent api for internal state manipulation.
 */
@Entity
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class Payment {

    public enum Type {TYPE1, TYPE2, TYPE3}

    public enum State {CREATED, CANCELLED}

    @Transient
    private Clock clock;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // Business is separate from technical DB id.
    // This also solves problem of equals and hashcode
    @Column(unique = true)
    private UUID paymentId = UUID.randomUUID();
    @Column(nullable = false)
    private Type type;
    @Column(nullable = false)
    @AttributeOverride(name="value", column=@Column(name="debtor_iban"))
    private Iban debtor;
    @Column(nullable = false)
    @AttributeOverride(name="value", column=@Column(name="creditor_iban"))
    private Iban creditor;
    @Column
    private Bic creditorBic;
    @Column(nullable = false)
    @Convert(converter = MonetaryAmountConverter.class)
    private MonetaryAmount amount;
    @Column(nullable = false)
    private LocalDateTime created;
    @Column(nullable = false)
    private State state = State.CREATED;
    @Column
    private String details;
    @Column
    @Convert(converter = MonetaryAmountConverter.class)
    private MonetaryAmount cancellationFee;

    // For testing purposes
    Payment(Clock clock, LocalDateTime created, Type type, Iban debtor, Iban creditor, Bic creditorBic, String details, MonetaryAmount amount) {
        this.clock = clock;
        this.created = created;
        this.type = type;
        this.debtor = debtor;
        this.creditor = creditor;
        this.creditorBic = creditorBic;
        this.details = details;
        this.amount = amount;
    }

    /**
     * Creation of payment is possible only through repository or factory.
     * This way we protect business logic invariants.
     */
    Payment(Clock clock, Type type, Iban debtor, Iban creditor, Bic creditorBic, String details, MonetaryAmount amount) {
        this(clock, LocalDateTime.now(clock), type, debtor, creditor, creditorBic, details, amount);
    }

    /**
     * Cancel payment with regards to business logic invariants (we can only cancel the same day payment was created)
     *
     * @param cancellationFeePolicy cancellation fee may apply according to policy
     * @return either error message or calculated fee value
     */
    public Either<ErrorMessage, MonetaryAmount> cancel(CancellationFeePolicy cancellationFeePolicy) {
        Objects.requireNonNull(cancellationFeePolicy, "Cannot cancel without fee policy");
        if (state == State.CANCELLED) {
            return Either.left(error("Cancellation failure. Payment already cancelled"));
        }

        var now = LocalDateTime.now(clock);
        var cancellationDeadline = created.plusDays(1).with(LocalTime.MIDNIGHT);
        if (now.isAfter(cancellationDeadline)) {
            return Either.left(error("Cancellation failure. Payment cancellation was possible before {0}", cancellationDeadline));
        }

        this.state = State.CANCELLED;
        var fee = cancellationFeePolicy.apply(created, type, amount);
        this.cancellationFee = fee;
        return Either.right(fee);
    }

    public Type getType() { return type; }

    UUID getPaymentId() {
        return paymentId;
    }

    /**
     * This is consequence of decision to mix aggregate with persistence information.
     * JPA is able to load object state from DB but dependency injection has to be done
     * manually in repository.
     */
    void setClock(Clock clock) {
        this.clock = clock;
    }

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

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", type=" + type +
                ", debtor=" + debtor +
                ", creditor=" + creditor +
                ", creditorBic=" + creditorBic +
                ", amount=" + amount +
                ", created=" + created +
                ", state=" + state +
                ", details=" + details +
                ", cancellationFee=" + cancellationFee +
                '}';
    }
}
