package org.dsinczak.paymentsprocessing.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

import static org.dsinczak.paymentsprocessing.web.MdcLoggingInterceptor.CORRELATION_ID_LOG_VAR_NAME;
import static org.springframework.http.HttpStatus.OK;

/**
 * This interceptor is a matter of big refactoring
 * - we can extract provider connector se we can configure it for tests
 * - we can extract client audit persisting strategy (currently writing to log) so w.g. it
 *   is possible to write this information to audit log
 * I assumed that clue of this exercise it to show that i'm not blocking main thread for something
 * that is clearly blocking cross cutting concern and can impact purchase service latency for no
 * reason.
 */
@Slf4j
public class ClientAuditInterceptor extends HandlerInterceptorAdapter {

    private static final String PROVIDER_SERVICE = "http://ip-api.com/json/";
    private static final Set<String> LOCAL_MACHINE = HashSet.of("127.0.0.1", "0.0.0.0");

    private final ExecutorService executorService;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ClientAuditInterceptor(ExecutorService executorService, HttpClient httpClient, ObjectMapper objectMapper) {
        this.executorService = executorService;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        var ip = request.getRemoteAddr();
        if(isLocalMachine(ip)) {
           log.info("Client ({}) call is from local machine. Skipping geolocation resolving.", ip);
        } else {
            callProvider(ip);
        }
        return true;
    }

    private boolean isLocalMachine(String ip) {
        return LOCAL_MACHINE.contains(ip);
    }

    private void callProvider(String ip) {
        var providerUri = URI.create(PROVIDER_SERVICE + ip);
        log.debug("Resolving client ({}) country code by calling {}", ip, providerUri);
        HttpRequest providerRequest = providerRequest(providerUri);
        // As we run on separate thread we need to pass correlationId
        // so we can correlate client information with rest of the conversation
        var correlationId = MDC.getMap().get(CORRELATION_ID_LOG_VAR_NAME);
        httpClient.sendAsync(providerRequest, HttpResponse.BodyHandlers.ofString())
                .whenCompleteAsync(handleProviderResponse(ip, correlationId), executorService);
    }

    private BiConsumer<HttpResponse<String>, Throwable> handleProviderResponse(String ip, Object correlationId) {
        return (providerResponse, providerCallError) -> {
            try {
                MDC.put(CORRELATION_ID_LOG_VAR_NAME, correlationId);
                if (providerResponse != null && providerResponse.statusCode() == OK.value()) {
                    var jsonMap = objectMapper.readValue(providerResponse.body(), Map.class);
                    log.info("Client ({}) country: {} (code: {})", ip, jsonMap.get("country"), jsonMap.get("countryCode"));
                }
                if (providerCallError != null) {
                    log.warn("Unable to resolve client (" + ip + ") country code. Provider call error.", providerCallError);
                }
            } catch (Exception e) {
                log.error("Error handling provider response", e);
            } finally {
                MDC.clear();
            }
        };
    }

    private HttpRequest providerRequest(URI providerUri) {
        return HttpRequest.newBuilder()
                .GET()
                .uri(providerUri)
                .timeout(httpClient.connectTimeout().orElse(Duration.ofSeconds(10)))
                .build();
    }

}
