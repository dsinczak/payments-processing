package org.dsinczak.paymentsprocessing.web;

import io.vavr.collection.Seq;
import io.vavr.control.Try;
import org.dsinczak.paymentsprocessing.shared.ErrorMessage;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;

@Component
public class ErrorMessageRenderer {

    public Seq<String> render(Seq<ErrorMessage> errorMessages) {
        return errorMessages.map(this::render);
    }

    public String render(ErrorMessage errorMessage) {
        if (errorMessage.hasArgs()) {
            Object[] argsAsStrings = Arrays
                    .stream(errorMessage.getArgs())
                    .map(Object::toString)
                    .toArray();
            return Try.of(() -> MessageFormat.format(errorMessage.getValue(), argsAsStrings))
                    .getOrElse(errorMessage.getValue());
        } else {
            return errorMessage.getValue();
        }
    }
}
