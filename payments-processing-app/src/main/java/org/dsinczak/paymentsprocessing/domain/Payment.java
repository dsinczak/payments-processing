package org.dsinczak.paymentsprocessing.domain;

import io.vavr.control.Either;
import io.vavr.control.Option;
import org.dsinczak.paymentsprocessing.shared.ErrorMessage;

import javax.money.MonetaryAmount;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

import static org.dsinczak.paymentsprocessing.shared.ErrorMessage.error;

public class Payment {

    enum Type {TYPE1, TYPE2, TYPE3}

    enum State {CREATED, CANCELLED}

    private Clock clock;

    // Business is is separate from technical DB id.
    // This also solves problem of equals and hashcode
    private UUID paymentId = UUID.randomUUID();
    private Type type;
    private Iban debtor;
    private Iban creditor;
    private Option<Bic> creditorBic;
    private MonetaryAmount amount;
    private LocalDateTime created;
    private State state = State.CREATED;
    private Option<String> details;
    private Option<MonetaryAmount> cancellationFee = Option.none();

    // For testing purposes
    Payment(Clock clock, LocalDateTime created, Type type, Iban debtor, Iban creditor, Bic creditorBic, String details, MonetaryAmount amount) {
        this.clock = clock;
        this.created = created;
        this.type = type;
        this.debtor = debtor;
        this.creditor = creditor;
        this.creditorBic = Option.of(creditorBic);
        this.details = Option.of(details);
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
            return Either.left(error("Cancellation failure. Payment cancellation was possible before {}", cancellationDeadline));
        }

        this.state = State.CANCELLED;
        var fee = cancellationFeePolicy.apply(created, type, amount);
        this.cancellationFee = Option.some(fee);
        return Either.right(fee);
    }

    UUID getPaymentId() {
        return paymentId;
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
