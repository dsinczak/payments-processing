package org.dsinczak.paymentsprocessing.api;

import lombok.Value;

@Value
public class MoneyDto {
    String amount;
    String currency;
}
