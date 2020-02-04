package org.dsinczak.paymentsprocessing.api;

import lombok.Value;

@Value
public class PaymentCancellationFeeDto {
    String paymentId;
    String cancellationFeeAmount;
    String cancellationFeeCurrency;
}
