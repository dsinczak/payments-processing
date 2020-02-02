package org.dsinczak.paymentsprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dsinczak.paymentsprocessing.domain.ByHourCancellationFeePolicy;
import org.dsinczak.paymentsprocessing.domain.CancellationFeePolicy;
import org.dsinczak.paymentsprocessing.domain.PaymentFactory;
import org.dsinczak.paymentsprocessing.domain.PaymentRepository;
import org.dsinczak.paymentsprocessing.web.ClientAuditInterceptor;
import org.dsinczak.paymentsprocessing.web.MdcLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class PaymentsConfiguration implements WebMvcConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    public Clock applicationClock() {
        // Clock should be always passed as dependency because this
        // increases testability
        return Clock.systemDefaultZone();
    }

    @Bean
    public CancellationFeePolicy cancellationFeePolicy() {
        return new ByHourCancellationFeePolicy(applicationClock());
    }

    @Bean
    public PaymentFactory paymentFactory() {
        return new PaymentFactory(applicationClock());
    }

    @Bean
    public PaymentRepository paymentRepository() {
        return new PaymentRepository();
    }

    @Bean
    public ExecutorService clientAuditExecutorService() { return Executors.newFixedThreadPool(3); }

    public HttpClient clientAuditHttpClient() {
        return HttpClient.newBuilder()
                .executor(clientAuditExecutorService())
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MdcLoggingInterceptor()).order(Ordered.HIGHEST_PRECEDENCE);
        registry.addInterceptor(new ClientAuditInterceptor(clientAuditExecutorService(), clientAuditHttpClient(), objectMapper)).order(Ordered.LOWEST_PRECEDENCE);
    }
}
