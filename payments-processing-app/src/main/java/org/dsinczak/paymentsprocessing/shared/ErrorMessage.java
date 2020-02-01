package org.dsinczak.paymentsprocessing.shared;

import lombok.Value;

@Value
public class ErrorMessage {

    public static ErrorMessage error(String value, Object ... args) {
        return new ErrorMessage(value, args);
    }

    private final String value;
    private final Object[] args;
}
