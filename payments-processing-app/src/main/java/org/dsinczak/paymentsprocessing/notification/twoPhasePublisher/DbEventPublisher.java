package org.dsinczak.paymentsprocessing.notification.twoPhasePublisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dsinczak.paymentsprocessing.api.events.PaymentEvent;
import org.dsinczak.paymentsprocessing.notification.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


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
    @Transactional(propagation = Propagation.REQUIRED)
    public void publish(PaymentEvent event) {
        try {
            log.debug("Storing event {} to database for further processing.", event);
            var eventJson = objectMapper.writeValueAsString(event);
            eventRepository.save(new DbEvent(eventJson));
        } catch (Exception e) {
            log.error("Unable to publish event: " + event, e);
        }
    }
}
