package org.dsinczak.paymentsprocessing;

import org.dsinczak.paymentsprocessing.domain.CancellationFeePolicy;
import org.dsinczak.paymentsprocessing.domain.HoursBasedCancellationFeePolicy;
import org.dsinczak.paymentsprocessing.domain.PaymentFactory;
import org.dsinczak.paymentsprocessing.domain.PaymentRepository;
import org.dsinczak.paymentsprocessing.web.MdcLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Configuration
public class PaymentsConfiguration implements WebMvcConfigurer {

    @Bean
    public Clock applicationClock() {
        // Clock should be always passed as dependency because this
        // increases testability
        return Clock.systemDefaultZone();
    }

    @Bean
    public CancellationFeePolicy cancellationFeePolicy() {
        return new HoursBasedCancellationFeePolicy();
    }

    @Bean
    public PaymentFactory paymentFactory() {
        return new PaymentFactory(applicationClock());
    }

    @Bean
    public PaymentRepository paymentRepository() {
        return new PaymentRepository();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MdcLoggingInterceptor());
    }
}
