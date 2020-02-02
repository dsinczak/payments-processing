package org.dsinczak.paymentsprocessing.api;

import lombok.Value;

import java.util.List;

@Value
public class ErrorDto {
    private final List<String> messages;

    public ErrorDto(String message) {
        this.messages = List.of(message);
    }
}
