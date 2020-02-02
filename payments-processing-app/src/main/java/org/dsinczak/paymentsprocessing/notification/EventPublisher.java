package org.dsinczak.paymentsprocessing.notification;

import org.dsinczak.paymentsprocessing.api.events.PaymentEvent;

public interface EventPublisher {
    void publish(PaymentEvent event);
}
