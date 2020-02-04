package org.dsinczak.paymentsprocessing.notification.twoPhasePublisher

import com.fasterxml.jackson.databind.ObjectMapper
import io.vavr.control.Option
import org.dsinczak.paymentsprocessing.api.events.PaymentCancelledEvent
import org.dsinczak.paymentsprocessing.api.events.PaymentCreatedEvent
import spock.lang.Shared
import spock.lang.Specification

class TwoPhaseEventPublishCaseSpec extends Specification {

    @Shared
    def objectMapper = ObjectMapper.newInstance()

    def eventRepository = Mock(DbEventRepository)

    def eventSenderConduit = Mock(DbEventSenderConduit)

    def 'should publish event to db'() {
        given:
            def event = new PaymentCancelledEvent("123")
            def publisher = new DbEventPublisher(eventRepository, objectMapper)
        when:
            publisher.publish(event)
        then:
            1 * eventRepository.save(new DbEvent(objectMapper.writeValueAsString(event)))
    }

    def 'should load event from repository and send using conduit'() {
        given:
            def event = new PaymentCreatedEvent("123", "TYPE1")
            def eventAsJson = objectMapper.writeValueAsString(event)
            eventRepository.findTop1ByOrderByIdAsc() >> Option.of(new DbEvent(eventAsJson))
            def eventSender = new DbEventSender(eventRepository, eventSenderConduit, objectMapper)
        when:
            eventSender.sendEvents()
        then:
            1 * eventSenderConduit.send(event)
            1 * eventRepository.delete(_)
    }
}
