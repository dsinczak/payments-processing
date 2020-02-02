package org.dsinczak.paymentsprocessing.api.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;
import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
public class PaymentCancelledEvent extends PaymentEvent {

    private final UUID paymentId;

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

}
