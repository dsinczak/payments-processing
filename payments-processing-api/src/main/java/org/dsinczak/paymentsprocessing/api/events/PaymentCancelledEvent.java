package org.dsinczak.paymentsprocessing.api.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public class PaymentCancelledEvent extends PaymentEvent {

    private final String paymentId;

    public PaymentCancelledEvent(String paymentId) {
        this.paymentId = paymentId;
    }

    @JsonCreator
    PaymentCancelledEvent(@JsonProperty("eventId") String eventId, @JsonProperty("paymentId") String paymentId) {
        super(eventId);
        this.paymentId = paymentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentCreatedEvent)) return false;
        PaymentCreatedEvent that = (PaymentCreatedEvent) o;
        return getEventId().equals(that.getEventId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }

    @Override
    public boolean needsConfirmation() {
        return false;
    }
}
