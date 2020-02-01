package org.dsinczak.paymentsprocessing.domain;

import io.vavr.control.Option;
import org.dsinczak.paymentsprocessing.shared.ErrorMessage;
import org.javamoney.moneta.Money;

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

    private UUID paymentId = UUID.randomUUID();
    private Type type;
    private Iban debtor;
    private Iban creditor;
    private Option<Bic> creditorBic;
    private MonetaryAmount amount;
    private LocalDateTime created;
    private State state = State.CREATED;
    private Option<String> details = Option.none();
    private Option<MonetaryAmount> cancellationFee = Option.none();

    // For testing purposes
    Payment(Clock clock, LocalDateTime created, Type type, Iban debtor, Iban creditor, Bic creditorBic, String details, MonetaryAmount amount) {
        this.clock = clock;
        this.created = created;
        this.type = type;
        this.debtor = debtor;
        this.creditor = creditor;
        this.creditorBic = Option.of(creditorBic);
        this.amount = amount;
    }

    /**
     * Creation of payment is possible only through repository or factory.
     * This way we protect business logic invariants.
     */
    Payment(Clock clock, Type type, Iban debtor, Iban creditor, Bic creditorBic, String details, MonetaryAmount amount) {
        this(clock, LocalDateTime.now(clock), type, debtor, creditor, creditorBic, details, amount);
    }

    public Option<ErrorMessage> cancel(CancellationFeePolicy cancellationFeePolicy) {
        Objects.requireNonNull(cancellationFeePolicy, "Cannot cancel without fee policy");
        if (state == State.CANCELLED) {
            return Option.of(error("Cancellation failure. Payment already cancelled"));
        }

        var now = LocalDateTime.now(clock);
        var cancellationDeadline = created.plusDays(1).with(LocalTime.MIDNIGHT);
        if (now.isAfter(cancellationDeadline)) {
            return Option.of(error("Cancellation failure. Payment cancellation was possible before {}", cancellationDeadline));
        }

        this.state = State.CANCELLED;
        this.cancellationFee = Option.some(cancellationFeePolicy.apply(created, type, amount));
        return Option.none();
    }

    public Type getType() {
        return type;
    }

    public Iban getDebtor() {
        return debtor;
    }

    public Iban getCreditor() {
        return creditor;
    }

    public Option<Bic> getCreditorBic() {
        return creditorBic;
    }

    public MonetaryAmount getAmount() {
        return amount;
    }

    public State getState() {
        return state;
    }

    public Option<MonetaryAmount> getCancellationFee() {
        return cancellationFee;
    }

    public Option<String> getDetails() { return details; }

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
