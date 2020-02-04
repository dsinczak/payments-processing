package org.dsinczak.paymentsprocessing.api.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@ToString
public class PaymentCreatedEvent extends PaymentEvent {

    private final String paymentId;

    private final String type;

    public PaymentCreatedEvent(String paymentId, String type) {
        this.paymentId = paymentId;
        this.type = type;
    }

    @JsonCreator
    PaymentCreatedEvent(@JsonProperty("eventId") String eventId, @JsonProperty("paymentId") String paymentId, @JsonProperty("type") String type) {
        super(eventId);
        this.paymentId = paymentId;
        this.type = type;
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
        return true;
    }
}
