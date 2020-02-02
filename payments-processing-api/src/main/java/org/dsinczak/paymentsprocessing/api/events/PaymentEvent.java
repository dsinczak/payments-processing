package org.dsinczak.paymentsprocessing.api.events;

import java.io.Serializable;
import java.util.UUID;

public abstract class PaymentEvent implements Serializable {

    private final UUID eventId = UUID.randomUUID();

    public abstract boolean needsConfirmation();

    public UUID getEventId() {
        return eventId;
    }
}
