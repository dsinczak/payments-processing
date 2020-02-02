package org.dsinczak.paymentsprocessing.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@EnableAsync
public class DbEventSender {

    private final DbEventRepository eventRepository;

    @Autowired
    public DbEventSender(DbEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Async
    @Scheduled(fixedDelay = 1000)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendEvents() {
        log.debug("Checking for not send events");
        //Option<DbEvent> dbEvent = eventRepository.findFirstNotSend();
    }
}
