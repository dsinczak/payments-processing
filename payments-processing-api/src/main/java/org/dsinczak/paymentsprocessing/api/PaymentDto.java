package org.dsinczak.paymentsprocessing.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentDto {

    private String type;
    private String debtorIban;
    private String creditorIban;
    private String creditorBic;
    private String amount;
    private String currency;
    private String details;

    @JsonCreator
    public PaymentDto(@JsonProperty("type") String type,
                      @JsonProperty("debtorIban") String debtorIban,
                      @JsonProperty("creditorIban") String creditorIban,
                      @JsonProperty("creditorBic") String creditorBic,
                      @JsonProperty("amount") String amount,
                      @JsonProperty("currency") String currency,
                      @JsonProperty("details") String details) {
        this.type = type;
        this.debtorIban = debtorIban;
        this.creditorIban = creditorIban;
        this.creditorBic = creditorBic;
        this.amount = amount;
        this.currency = currency;
        this.details = details;
    }


}
