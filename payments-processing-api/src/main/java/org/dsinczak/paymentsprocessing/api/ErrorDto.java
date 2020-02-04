package org.dsinczak.paymentsprocessing.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ErrorDto {
    @JsonProperty
    private final List<String> messages;

    @JsonCreator
    public ErrorDto(List<String> messages) {
        this.messages = messages;
    }

    public ErrorDto(String message) {
        this.messages = List.of(message);
    }
}
