package org.dsinczak.paymentsprocessing.domain


import org.javamoney.moneta.Money
import spock.lang.Specification
import spock.lang.Unroll

import javax.money.MonetaryAmount
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

import static io.vavr.control.Option.none
import static io.vavr.control.Option.some
import static org.dsinczak.paymentsprocessing.domain.Payment.Type.TYPE1
import static org.dsinczak.paymentsprocessing.shared.ErrorMessage.error

class PaymentCaseSpec extends Specification {

    static def zoneId = ZoneId.systemDefault()

    static def clock(int year, int month, int dayOfMonth, int hour, int minute) {
        Clock.fixed(LocalDateTime.of(year, month, dayOfMonth, hour, minute).atZone(zoneId).toInstant(), zoneId)
    }
    static def amount = Money.of(BigDecimal.TEN, "USD")
    static def iban = new Iban("123456789")
    static def bic = new Bic("666")
    static def constantPolicy = new CancellationFeePolicy() {
        @Override
        Money apply(LocalDateTime localDateTime, Payment.Type type, MonetaryAmount money) {
            return amount
        }
    }

    @Unroll
    def "should handle payment cancellation depending on creation and cancellation time"() {
        given:
            def payment = new Payment(now, createTime, TYPE1, iban, iban, bic, "",amount)
        when:
            def cancellationResult = payment.cancel(constantPolicy)
        then:
            cancellationResult == expectedResult
        where:
            now                         | createTime                             | expectedResult
            clock(1985, 02, 19, 16, 10) | LocalDateTime.of(1985, 02, 19, 16, 10) | none()
            clock(1985, 02, 19, 16, 10) | LocalDateTime.of(1985, 02, 18, 16, 10) | some(error("Cancellation failure. Payment cancellation was possible before {}", LocalDateTime.of(1985, 02, 19, 00, 00)))
            clock(1985, 02, 19, 23, 59) | LocalDateTime.of(1985, 02, 19, 16, 10) | none()
            clock(1985, 02, 20, 0, 0)   | LocalDateTime.of(1985, 02, 19, 16, 10) | none()
            clock(1985, 02, 20, 0, 1)   | LocalDateTime.of(1985, 02, 19, 16, 10) | some(error("Cancellation failure. Payment cancellation was possible before {}", LocalDateTime.of(1985, 02, 20, 00, 00)))
    }

    def "should apply cancellation fee policy"() {
        given:
            def payment = new Payment(clock(1985, 02, 19, 16, 10), LocalDateTime.of(1985, 02, 19, 16, 10), TYPE1, iban, iban, bic, "",amount)
        when:
            payment.cancel(constantPolicy)
        then:
            payment.getCancellationFee() == some(amount)
    }

    def "should fail to cancel payment twice"() {
        given:
            def payment = new Payment(clock(1985, 02, 19, 16, 10), LocalDateTime.of(1985, 02, 19, 16, 10), TYPE1, iban, iban, bic, "", amount)
        when:
            def firstCancel = payment.cancel(constantPolicy)
            def secondCancel = payment.cancel(constantPolicy)
        then:
            firstCancel.isEmpty()
            secondCancel == some(error("Cancellation failure. Payment already cancelled"))
    }


}
