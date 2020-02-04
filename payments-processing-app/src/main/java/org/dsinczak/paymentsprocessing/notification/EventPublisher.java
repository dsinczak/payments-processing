package org.dsinczak.paymentsprocessing.notification;

import org.dsinczak.paymentsprocessing.api.events.PaymentEvent;

@FunctionalInterface
public interface EventPublisher {
    void publish(PaymentEvent event);
}
