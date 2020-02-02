package org.dsinczak.paymentsprocessing.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dsinczak.paymentsprocessing.api.events.PaymentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This event publisher stores event in database. This way published event
 * is persisted and can by send asynchronously by separate mechanism implemented in
 * {@link DbEventSender}.
 * Important fact is that we do it in single transaction and no 2-phase commit needs
 * to take place. {@link DbEventSender} will send in separate transaction so this
 * way we do not block service for event sending and do not create time coupling between
 * service availability and event sending.
 */
@Slf4j
@Component
public class DbEventPublisher implements EventPublisher {

    private final DbEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DbEventPublisher(DbEventRepository eventRepository, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(PaymentEvent event) {
        try {
            var eventJson = objectMapper.writeValueAsString(event);
            eventRepository.save(new DbEvent(event.getEventId(), eventJson));
        } catch (Exception e) {
            log.error("Unable to publish event: " + event, e);
        }
    }
}
