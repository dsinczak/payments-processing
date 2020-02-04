package org.dsinczak.paymentsprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dsinczak.paymentsprocessing.api.events.PaymentEvent;
import org.dsinczak.paymentsprocessing.domain.*;
import org.dsinczak.paymentsprocessing.notification.twoPhasePublisher.DbEventSenderConduit;
import org.dsinczak.paymentsprocessing.web.ClientAuditInterceptor;
import org.dsinczak.paymentsprocessing.web.MdcLoggingInterceptor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
@EnableAsync
@Slf4j
public class PaymentsConfiguration implements WebMvcConfigurer, AsyncConfigurer {

    @Autowired
    private ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager entityManager;

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
        return new JpaPaymentRepository(entityManager, applicationClock());
    }

    @Bean
    public ExecutorService clientAuditExecutorService() {
        return Executors.newFixedThreadPool(3);
    }

    @Bean
    public HttpClient clientAuditHttpClient() {
        return HttpClient.newBuilder()
                .executor(clientAuditExecutorService())
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Bean
    public DbEventSenderConduit dbEventSenderConduit() {
        return (PaymentEvent e) -> log.info("Sending event {} to destination", e);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MdcLoggingInterceptor()).order(Ordered.HIGHEST_PRECEDENCE);
        registry.addInterceptor(new ClientAuditInterceptor(clientAuditExecutorService(), clientAuditHttpClient(), objectMapper)).order(Ordered.LOWEST_PRECEDENCE);
    }


    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("AsyncTaskExecutor-");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return ((throwable, method, objects) -> log.error("Async method " + method + "((" + Arrays.toString(objects) + ")) invocation error", throwable));
    }
}
