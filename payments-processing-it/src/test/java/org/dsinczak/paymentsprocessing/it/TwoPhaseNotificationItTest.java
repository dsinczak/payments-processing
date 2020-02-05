package org.dsinczak.paymentsprocessing.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dsinczak.paymentsprocessing.api.PaymentDto;
import org.dsinczak.paymentsprocessing.api.events.PaymentCreatedEvent;
import org.dsinczak.paymentsprocessing.notification.twoPhasePublisher.DbEventSenderConduit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TwoPhaseNotificationItTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    DbEventSenderConduit eventSenderConduit;

    @Test
    public void shouldSendNotificationUsing2PhaseApproach() throws Exception {
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
        mockMvc.perform(
                post("/payment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then - wait for seconds phase (sending)
        TimeUnit.SECONDS.sleep(3);
        // and -verify if event was send
        verify(eventSenderConduit).send(any(PaymentCreatedEvent.class));
    }
}
