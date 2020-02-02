package org.dsinczak.paymentsprocessing.api;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentDto {
    private final String type;
    private final String debtorIban;
    private final String creditorIban;
    private final String creditorBic;
    private final String amount;
    private final String currency;
    private final String details;
}
