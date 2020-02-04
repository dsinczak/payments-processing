package org.dsinczak.paymentsprocessing.domain;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import org.dsinczak.paymentsprocessing.shared.ErrorMessage;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.time.Clock;

import static io.vavr.control.Validation.combine;

/**
 * {@link Payment} aggregate factory. Covers invariants of payment creation behind interface.
 * More: https://medium.com/withbetterco/using-aggregates-and-factories-in-domain-driven-design-34e0dff220c3
 */
public class PaymentFactory {

    private static final CurrencyUnit USD = Monetary.getCurrency("USD");
    private static final CurrencyUnit EUR = Monetary.getCurrency("EUR");

    private final Clock applicationClock;

    public PaymentFactory(Clock applicationClock) {
        this.applicationClock = applicationClock;
    }

    public PaymentBuilder create() {
        return new PaymentBuilder();
    }

    /**
     * Functional composition of validations for TYPE1 and TYPE2 of payment
     */
    private Validation<Seq<ErrorMessage>, Payment> validateBasicType(Payment.Type type, PaymentBuilder builder, CurrencyUnit currency) {
        return combine(
                Validations.validateAmount(builder.currency, builder.amount, HashSet.of(currency)),
                Validations.validateIban("Debtor", builder.debtor),
                Validations.validateIban("Creditor", builder.creditor),
                Validations.validateDetails(builder.details))
                .ap((amount, debtor, creditor, details) -> new Payment(applicationClock, type, debtor, creditor, null, details, amount));
    }

    /**
     * Functional composition of validations for TYPE3 of payment
     */
    private Validation<Seq<ErrorMessage>, Payment> validateType3(PaymentBuilder builder) {
        return combine(
                Validations.validateAmount(builder.currency, builder.amount, HashSet.of(USD, EUR)),
                Validations.validateIban("Debtor", builder.debtor),
                Validations.validateIban("Creditor", builder.creditor),
                Validations.validateBic("Creditor", builder.creditorBic)
        ).ap((amount, debtor, creditor, bic) -> new Payment(applicationClock, Payment.Type.TYPE3, debtor, creditor, bic, null, amount));
    }

    public class PaymentBuilder {
        private String type;
        private String debtor;
        private String creditor;
        private String creditorBic;
        private String amount;
        private String currency;
        private String details;

        PaymentBuilder() {
        }

        public PaymentBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public PaymentBuilder withDebtor(String debtor) {
            this.debtor = debtor;
            return this;
        }

        public PaymentBuilder withCreditor(String creditor) {
            this.creditor = creditor;
            return this;
        }

        public PaymentBuilder withCreditorBic(String creditorBic) {
            this.creditorBic = creditorBic;
            return this;
        }

        public PaymentBuilder withAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public PaymentBuilder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public PaymentBuilder withDetails(String details) {
            this.details = details;
            return this;
        }

        public Either<Seq<ErrorMessage>, Payment> build() {
            /*
             * I'm aware of JSR-303 (https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-validation)
             * But what i do not like about it is its implicit behaviour (a.k.a exception throwing, also known as modern goto statement)
             * Type returned by build(): Either<Seq<ErrorMessage>, Payment> exactly suggests what is the
             * expected result: either creation error or payment. Another thing is
             */
            return Validations.validateType(this.type)
                    .<Seq<ErrorMessage>>mapError(List::of)
                    .flatMap(this::matchValidationType)
                    .toEither();
        }

        private Validation<Seq<ErrorMessage>, Payment> matchValidationType(Payment.Type type) {
            switch (type) {
                case TYPE1:
                    return validateBasicType(type, this, USD);
                case TYPE2:
                    return validateBasicType(type, this, EUR);
                case TYPE3:
                    return validateType3(this);
                default:
                    throw new IllegalArgumentException("Payment type " + type + " is not supported.");
            }
        }
    }

}
