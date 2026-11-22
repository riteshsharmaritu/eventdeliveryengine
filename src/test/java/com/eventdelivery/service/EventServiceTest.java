package com.eventdelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventdelivery.domain.Event;
import com.eventdelivery.domain.EventType;
import com.eventdelivery.domain.ResponseStatus;
import com.eventdelivery.dto.PaymentEventRequest;
import com.eventdelivery.dto.PaymentEventResponse;
import com.eventdelivery.repository.EventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void receivePaymentEventPersistsEventAndReturnsSuccess() {
        PaymentEventRequest request = new PaymentEventRequest();
        request.setPaymentId("pay-100");
        request.setAmount(new BigDecimal("25.50"));
        request.setDate(Instant.parse("2026-01-15T12:00:00Z"));
        request.setInvoiceId("inv-1");
        request.setMerchant("acme");
        request.setEventType(EventType.PARTIALLY_PAID);

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setEventId(10L);
            return event;
        });

        PaymentEventResponse response = eventService.receivePaymentEvent(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        assertThat(response.getDate()).isNotNull();

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getPaymentId()).isEqualTo("pay-100");
        assertThat(captor.getValue().getMerchant()).isEqualTo("acme");
        assertThat(captor.getValue().getEventType()).isEqualTo(EventType.PARTIALLY_PAID);
        assertThat(captor.getValue().getAmount()).isEqualByComparingTo("25.50");
    }
}
