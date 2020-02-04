package org.dsinczak.paymentsprocessing.domain;

import io.vavr.collection.Set;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import org.dsinczak.paymentsprocessing.shared.ErrorMessage;
import org.javamoney.moneta.Money;

import javax.money.CurrencyUnit;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import static io.vavr.control.Validation.invalid;
import static io.vavr.control.Validation.valid;
import static org.dsinczak.paymentsprocessing.shared.ErrorMessage.error;

/**
 * Container namespace for business validations. Validations implemented here are the smallest
 * compose-able functions. The whole idea, it that we can compose validation of incoming requests
 * from small testable pieces.
 */
abstract class Validations {

    private static final Pattern IBAN_PATTERN = Pattern.compile("[a-zA-Z]{2}[0-9]{2}[a-zA-Z0-9]{4}[0-9]{7}([a-zA-Z0-9]?){0,16}");
    private static final Pattern BIC_PATTERN = Pattern.compile("[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}");

    private Validations() {
    }

    static Validation<ErrorMessage, String> validateDetails(String details) {
        if (details == null) return invalid(error("Details are required"));
        return details.isBlank()
                ? invalid(error("Payment details cannot be empty string"))
                : valid(details);
    }

    static Validation<ErrorMessage, Money> validateAmount(String currency, String amount, Set<CurrencyUnit> expectedCurrencies) {
        if (currency == null || amount == null) return invalid(error("Amount and currency are required"));
        return Try.of(() -> new BigDecimal(amount))
                .filter(a -> a.compareTo(BigDecimal.ZERO) > 0)
                .map(a -> Money.of(a, currency))
                .filter(m -> expectedCurrencies.contains(m.getCurrency()))
                .map(Validation::<ErrorMessage, Money>valid)
                .getOrElse(invalid(error("Amount {0} {1} is not valid positive decimal with expected currency {2}", amount, currency, expectedCurrencies)));
    }

    static Validation<ErrorMessage, Payment.Type> validateType(String type) {
        if (type == null) return invalid(error("Payment type is required"));
        return Try.of(() -> Payment.Type.valueOf(type))
                .map(Validation::<ErrorMessage, Payment.Type>valid)
                .getOrElse(() -> invalid(error("Payment type {0} is invalid. Supported types {1}.", type, Payment.Type.values())));
    }

    static Validation<ErrorMessage, Iban> validateIban(String owner, String iban) {
        if (iban == null) return invalid(error("{0} IBAN is required", owner));
        return IBAN_PATTERN.matcher(iban).matches()
                ? valid(new Iban(iban))
                : invalid(error("{0} IBAN does not match pattern: {1}", owner, IBAN_PATTERN.pattern()));
    }

    static Validation<ErrorMessage, Bic> validateBic(String owner, String bic) {
        if (bic == null) return invalid(error("{0} BIC is required", owner));
        return BIC_PATTERN.matcher(bic).matches()
                ? valid(new Bic(bic))
                : invalid(error("{0} BIC does not match pattern: {1}", owner, BIC_PATTERN.pattern()));
    }
}
