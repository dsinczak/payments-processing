package org.dsinczak.paymentsprocessing.domain

import org.javamoney.moneta.Money
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

import static org.dsinczak.paymentsprocessing.domain.Payment.Type.*

class ByHourCancellationFeePolicyCaseSpec extends Specification {

    static def zoneId = ZoneId.systemDefault()

    static now = Clock.fixed(LocalDateTime.of(1985, 2, 19, 16, 0).atZone(zoneId).toInstant(), zoneId)

    def 'should calculate fee depending on hours and payment type'() {
        given:
            def policy = new ByHourCancellationFeePolicy(now)
        when:
            def fee = policy.apply(created, type, null)
        then:
            fee == expectedFee
        where:
            created                               | type  | expectedFee
            LocalDateTime.of(1985, 2, 19, 14, 10) | TYPE1 | Money.of(0.05, "EUR")
            LocalDateTime.of(1985, 2, 19, 14, 10) | TYPE2 | Money.of(0.1, "EUR")
            LocalDateTime.of(1985, 2, 19, 14, 10) | TYPE3 | Money.of(0.15, "EUR")
            LocalDateTime.of(1985, 2, 19, 1, 50)  | TYPE1 | Money.of(0.7, "EUR")
            LocalDateTime.of(1985, 2, 19, 3, 30)  | TYPE2 | Money.of(1.2, "EUR")
            LocalDateTime.of(1985, 2, 19, 10, 10) | TYPE3 | Money.of(0.75, "EUR")
    }

    def 'should fail when creation date is after current time'() {
        given:
            def policy = new ByHourCancellationFeePolicy(now)
        when:
            policy.apply(LocalDateTime.of(1985, 2, 19, 19, 10), TYPE1, null)
        then:
            thrown(IllegalArgumentException)
    }
}
