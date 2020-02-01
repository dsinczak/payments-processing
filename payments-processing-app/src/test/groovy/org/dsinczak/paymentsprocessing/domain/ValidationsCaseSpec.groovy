package org.dsinczak.paymentsprocessing.domain

import io.vavr.collection.HashSet
import spock.lang.Specification

import javax.money.CurrencyUnit
import javax.money.Monetary

class ValidationsCaseSpec extends Specification {

    private static final CurrencyUnit USD = Monetary.getCurrency("USD");
    private static final CurrencyUnit EUR = Monetary.getCurrency("EUR");

    def 'should validate IBAN'() {
        expect:
            Validations.validateIban("Damian", iban).isValid() == isValid
        where:
            iban                          | isValid
            "SE3550000000054910000003"    | true
            "CH9300762011623852957"       | true
            "HU4211773016111110180000000" | true
            "DE89370400440532013000"      | true
            "9E3550000000054910000003"    | false
            "C59300762011623852957"       | false
            "HU42117"                     | false
            "DE89370400ABC532013000"      | false
            null                          | false
    }

    def 'should validate details'() {
        expect:
            Validations.validateDetails(details).isValid() == isValid
        where:
            details  | isValid
            "Damian" | true
            ""       | false
            "      " | false
            null     | false
    }

    def 'should validate amount'() {
        expect:
            Validations.validateAmount(currency, amount, HashSet.of(EUR, USD)).isValid() == isValid
        where:
            currency | amount  | isValid
            "USD"    | "12.23" | true
            "EUR"    | "12.23" | true
            "CHY"    | "12.23" | false
            "PLN"    | "1.0"   | false
            "EUR"    | "-10.9" | false
            "EUR"    | "ABC"   | false
            null     | "12.23" | false
            "EUR"    | null    | false
            null     | null    | false
    }

    def 'should validate type'() {
        expect:
            Validations.validateType(type).isValid() == isValid
        where:
            type    | isValid
            "TYPE1" | true
            "TYPE2" | true
            "TYPE3" | true
            "TYPE4" | false
            null    | false
    }

}
