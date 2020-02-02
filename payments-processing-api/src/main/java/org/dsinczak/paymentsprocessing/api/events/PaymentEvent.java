package org.dsinczak.paymentsprocessing.api.events;

import java.util.UUID;

public abstract class PaymentEvent {

    private final UUID eventId = UUID.randomUUID();

    public UUID getEventId() {
        return eventId;
    }
}
