package org.dsinczak.paymentsprocessing.api.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(use = NAME, include = PROPERTY, property = "event-type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = PaymentCreatedEvent.class, name = "paymentCreatedEvent"),
        @JsonSubTypes.Type(value = PaymentCancelledEvent.class, name = "paymentCancelledEvent"),

})
public abstract class PaymentEvent implements Serializable {

    @JsonProperty
    private final String eventId;

    public PaymentEvent() {
        this.eventId = UUID.randomUUID().toString();
    }

    protected PaymentEvent(String eventId) {
        this.eventId = eventId;
    }

    public abstract boolean needsConfirmation();

    public String getEventId() {
        return eventId;
    }
}
