package org.dsinczak.paymentsprocessing.api;

import lombok.Value;

import java.util.List;

@Value
public class PaymentIdsDto {

    List<String> ids;

}
