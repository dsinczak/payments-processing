package org.dsinczak.paymentsprocessing.notification.twoPhasePublisher;

import org.dsinczak.paymentsprocessing.api.events.PaymentEvent;

@FunctionalInterface
public interface DbEventSenderConduit {

    void send(PaymentEvent event);

}
