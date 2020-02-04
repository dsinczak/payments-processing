package org.dsinczak.paymentsprocessing.notification.twoPhasePublisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dsinczak.paymentsprocessing.api.events.PaymentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class DbEventSender {

    private final DbEventRepository eventRepository;
    private final DbEventSenderConduit senderConduit;
    private final ObjectMapper objectMapper;

    @Autowired
    public DbEventSender(DbEventRepository eventRepository, DbEventSenderConduit senderConduit, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.senderConduit = senderConduit;
        this.objectMapper = objectMapper;
    }

    @Async
    @Scheduled(fixedDelay = 1000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEvents() {
        eventRepository.findTop1ByOrderByIdAsc().forEach(this::send);
    }

    private void send(DbEvent dbEvent) {
        log.debug("Found event to send {}", dbEvent);
        try {
            var event = objectMapper.readValue(dbEvent.event, PaymentEvent.class);
            senderConduit.send(event);
            eventRepository.delete(dbEvent);
        } catch (Exception e) {
            log.error("Error sending event to destination, another attempt soon.", e);
        }
    }
}
