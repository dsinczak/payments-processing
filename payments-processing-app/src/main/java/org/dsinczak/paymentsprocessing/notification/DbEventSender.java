package org.dsinczak.paymentsprocessing.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    public void sendEvents() {
        log.debug("Checking for not send events");
    }
}
