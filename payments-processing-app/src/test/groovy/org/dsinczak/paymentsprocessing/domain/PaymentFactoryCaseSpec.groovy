package org.dsinczak.paymentsprocessing.domain

import io.vavr.collection.List
import spock.lang.Shared
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

import static org.dsinczak.paymentsprocessing.shared.ErrorMessage.error

class PaymentFactoryCaseSpec extends Specification {

    static def zoneId = ZoneId.systemDefault()

    static def now = Clock.fixed(LocalDateTime.of(1985, 2, 19, 16, 10).atZone(zoneId).toInstant(), zoneId)

    @Shared
    def factory = new PaymentFactory(now)

    def 'should fail then type is unknown'() {
        when:
            def payment = factory.create()
                    .withType("TYPE666")
                    .withDetails("Make the money rain")
                    .build()
        then:
            payment.getLeft() == List.of(error("Payment type {} is invalid. Supported types {}.", "TYPE666", Payment.Type.values()))
    }

    def 'should create TYPE1 payment'() {
        when:
            def payment = factory.create()
                    .withType("TYPE1")
                    .withAmount("10")
                    .withCurrency("USD")
                    .withCreditor("SE3550000000054910000003")
                    .withDebtor("CH9300762011623852957")
                    .withDetails("Make the money rain")
                    .build()
        then:
            payment.isRight()
    }

    def 'should fail for TYPE1 with wrong currency'() {
        when:
            def payment = factory.create()
                    .withType("TYPE1")
                    .withAmount("10")
                    .withCurrency("EUR")
                    .withCreditor("SE3550000000054910000003")
                    .withDebtor("CH9300762011623852957")
                    .withDetails("Make the money rain")
                    .build()
        then:
            payment.isLeft()
    }

    def 'should fail returning multiple error messages for no description and wrong currency'() {
        when:
            def payment = factory.create()
                    .withType("TYPE1")
                    .withAmount("10")
                    .withCurrency("PLN")
                    .withCreditor("SE3550000000054910000003")
                    .withDebtor("CH9300762011623852957")
                    .build()
        then:
            payment.isLeft()
            payment.left.size() == 2
    }

    def 'should create TYPE2 payment'() {
        when:
            def payment = factory.create()
                    .withType("TYPE2")
                    .withAmount("10")
                    .withCurrency("EUR")
                    .withCreditor("SE3550000000054910000003")
                    .withDebtor("CH9300762011623852957")
                    .withDetails("Make the money rain")
                    .build()
        then:
            payment.isRight()
    }

    def 'should fail for TYPE2 with wrong creditor'() {
        when:
            def payment = factory.create()
                    .withType("TYPE2")
                    .withAmount("10")
                    .withCurrency("EUR")
                    .withCreditor("SOMETHING_WENT_WRONG")
                    .withDebtor("CH9300762011623852957")
                    .withDetails("Make the money rain")
                    .build()
        then:
            payment.isLeft()
    }

    def 'should create TYPE3 payment'() {
        when:
            def payment = factory.create()
                    .withType("TYPE3")
                    .withAmount("10")
                    .withCurrency("EUR")
                    .withCreditor("SE3550000000054910000003")
                    .withDebtor("CH9300762011623852957")
                    .withCreditorBic("EBOSPLPW")
                    .build()
        then:
            payment.isRight()
    }

    def 'should fail creating TYPE3 payment without BIC'() {
        when:
            def payment = factory.create()
                    .withType("TYPE3")
                    .withAmount("10")
                    .withCurrency("EUR")
                    .withCreditor("SE3550000000054910000003")
                    .withDebtor("CH9300762011623852957")
                    .build()
        then:
            payment.isLeft()
    }
}
