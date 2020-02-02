package org.dsinczak.paymentsprocessing.web;

import io.vavr.control.Option;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public class MdcLoggingInterceptor extends HandlerInterceptorAdapter {
    private static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-Id";
    static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.put(CORRELATION_ID_LOG_VAR_NAME,
                Option.of(request.getHeader(CORRELATION_ID_HEADER_NAME))
                        .filter(h -> !h.isBlank())
                        .getOrElse(() -> UUID.randomUUID().toString())
        );
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(CORRELATION_ID_LOG_VAR_NAME);
    }
}
