package org.dsinczak.paymentsprocessing.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dsinczak.paymentsprocessing.api.ErrorDto;
import org.dsinczak.paymentsprocessing.api.MoneyDto;
import org.dsinczak.paymentsprocessing.api.PaymentCancellationFeeDto;
import org.dsinczak.paymentsprocessing.api.PaymentDto;
import org.dsinczak.paymentsprocessing.api.events.PaymentCreatedEvent;
import org.dsinczak.paymentsprocessing.domain.Payment;
import org.dsinczak.paymentsprocessing.domain.PaymentRepository;
import org.dsinczak.paymentsprocessing.notification.EventPublisher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerItTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PaymentRepository paymentRepository;

    @MockBean
    EventPublisher eventPublisher;

    @Test
    public void shouldCreatePaymentOfTYPE1AndSendEvent() throws Exception {
        // Given
        PaymentDto paymentDto = PaymentDto.builder()
                .type("TYPE1")
                .amount("10")
                .currency("USD")
                .creditorIban("SE3550000000054910000003")
                .debtorIban("CH9300762011623852957")
                .details("some details")
                .build();

        // When
        var paymentId = mockMvc.perform(
                post("/payment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - payment exists in DB
        var paymentOpt = paymentRepository.findByPaymentId(UUID.fromString(paymentId));
        assertThat(paymentOpt.isDefined()).isEqualTo(true);
        var payment = paymentOpt.get();
        assertThat(payment.getType()).isEqualTo(Payment.Type.TYPE1);
        // And - event was published
        verify(eventPublisher).publish(any(PaymentCreatedEvent.class));
    }

    @Test
    public void shouldCreatePaymentOfTYPE3AndDoNotSendEvent() throws Exception {
        // Given
        PaymentDto paymentDto = PaymentDto.builder()
                .type("TYPE3")
                .amount("10")
                .currency("USD")
                .creditorIban("SE3550000000054910000003")
                .debtorIban("CH9300762011623852957")
                .creditorBic("EBOSPLPW")
                .build();

        // When
        var paymentId = mockMvc.perform(
                post("/payment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - payment exists in DB
        var paymentOpt = paymentRepository.findByPaymentId(UUID.fromString(paymentId));
        assertThat(paymentOpt.isDefined()).isEqualTo(true);
        var payment = paymentOpt.get();
        assertThat(payment.getType()).isEqualTo(Payment.Type.TYPE3);
        // And - event was published
        verify(eventPublisher, never()).publish(any(PaymentCreatedEvent.class));
    }

    @Test
    public void shouldFailToCreatePaymentForWrongIban() throws Exception {
        // Given
        PaymentDto paymentDto = PaymentDto.builder()
                .type("TYPE3")
                .amount("10")
                .currency("USD")
                .creditorIban("BAD_IBAN")
                .debtorIban("EVEN_WORSE")
                .creditorBic("EBOSPLPW")
                .build();

        // When
        var errorMessage = mockMvc.perform(
                post("/payment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ErrorDto errors = objectMapper.readValue(errorMessage, ErrorDto.class);
        assertThat(errors.getMessages()).containsAll(List.of(
                "Debtor IBAN does not match pattern: [a-zA-Z]{2}[0-9]{2}[a-zA-Z0-9]{4}[0-9]{7}([a-zA-Z0-9]?){0,16}",
                "Creditor IBAN does not match pattern: [a-zA-Z]{2}[0-9]{2}[a-zA-Z0-9]{4}[0-9]{7}([a-zA-Z0-9]?){0,16}"
        ));
    }

    @Test
    public void shouldCancelExistingPayment() throws Exception {
        // Given
        PaymentDto paymentDto = PaymentDto.builder()
                .type("TYPE2")
                .amount("10")
                .currency("EUR")
                .creditorIban("SE3550000000054910000003")
                .debtorIban("CH9300762011623852957")
                .details("some details")
                .build();

        // And
        var paymentId = mockMvc.perform(
                post("/payment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // When
        var fee = mockMvc.perform(put("/payment/{paymentId}/cancellation", paymentId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - cancellation amount is returned
        var feeMoney = objectMapper.readValue(fee, MoneyDto.class);
        assertThat(feeMoney.getAmount()).isEqualTo("0");
        assertThat(feeMoney.getCurrency()).isEqualTo("EUR");

        // And - we can retrieve payment fee entity
        var paymentFee = mockMvc.perform(get("/payment/{paymentId}/cancellation", paymentId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var paymentFeeDto = objectMapper.readValue(paymentFee, PaymentCancellationFeeDto.class);

        assertThat(paymentFeeDto.getCancellationFeeAmount()).isEqualTo("0");
        assertThat(paymentFeeDto.getCancellationFeeCurrency()).isEqualTo("EUR");
    }

    @Test
    public void shouldFailToCancelPaymentThatDoesNotExist() throws Exception {
        // Given
        var paymentId =  UUID.randomUUID().toString();

        // When
        var errorMessage = mockMvc.perform(put("/payment/{paymentId}/cancellation", paymentId))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        //Then
        ErrorDto errors = objectMapper.readValue(errorMessage, ErrorDto.class);
        assertThat(errors.getMessages()).contains("Payment with ID: "+paymentId+" does not exist.");
    }


}
