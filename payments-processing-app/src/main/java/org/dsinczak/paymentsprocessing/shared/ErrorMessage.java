package org.dsinczak.paymentsprocessing.shared;

import io.vavr.control.Option;
import lombok.Value;

@Value
public class ErrorMessage {

    public static ErrorMessage error(String value, Object ... args) {
        return new ErrorMessage(value, args);
    }

    // in more production world we shouldn't pass error message but error message code with
    // parameters. So end client can render message for himself.
    private final String value;
    private final Object[] args;

    public boolean hasArgs() {
        return Option.of(args).map(a->a.length>0).getOrElse(false);
    }
}
