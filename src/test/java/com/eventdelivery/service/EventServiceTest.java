package com.eventdelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventdelivery.client.WebhookClient;
import com.eventdelivery.domain.DeliveryStatus;
import com.eventdelivery.domain.Event;
import com.eventdelivery.domain.EventDelivery;
import com.eventdelivery.domain.EventType;
import com.eventdelivery.domain.ResponseStatus;
import com.eventdelivery.domain.Tenant;
import com.eventdelivery.dto.PaymentEventRequest;
import com.eventdelivery.dto.PaymentEventResponse;
import com.eventdelivery.dto.WebhookPayload;
import com.eventdelivery.repository.EventDeliveryRepository;
import com.eventdelivery.repository.EventRepository;
import com.eventdelivery.repository.TenantRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    private static final String WEBHOOK_URL = "http://tenant.example/webhook";
    private static final String MERCHANT = "acme";

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EventDeliveryRepository eventDeliveryRepository;

    @Mock
    private WebhookClient webhookClient;

    @InjectMocks
    private EventService eventService;

    private PaymentEventRequest request;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        request = new PaymentEventRequest();
        request.setPaymentId("pay-100");
        request.setAmount(new BigDecimal("25.50"));
        request.setDate(Instant.parse("2026-01-15T12:00:00Z"));
        request.setInvoiceId("inv-1");
        request.setMerchant(MERCHANT);
        request.setEventType(EventType.FULLY_PAID);

        tenant = new Tenant();
        tenant.setTenantId(1L);
        tenant.setUrl(WEBHOOK_URL);
        tenant.setMerchant(MERCHANT);
        tenant.setRetriesNo(2);
        tenant.setDateAdded(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void deliversEventSuccessfullyInRealTime() {
        stubNewEventAndTenant();
        when(webhookClient.invoke(eq(WEBHOOK_URL), any(WebhookPayload.class))).thenReturn(true);

        PaymentEventResponse response = eventService.receivePaymentEvent(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        verify(webhookClient, times(1)).invoke(eq(WEBHOOK_URL), any(WebhookPayload.class));

        List<EventDelivery> deliveries = capturedDeliveries();
        assertThat(deliveries).hasSize(1);
        assertThat(deliveries.get(0).getRetryNo()).isEqualTo(1);
        assertThat(deliveries.get(0).getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void retriesOnFailureAndEventuallySucceeds() {
        stubNewEventAndTenant();
        when(webhookClient.invoke(eq(WEBHOOK_URL), any(WebhookPayload.class)))
                .thenReturn(false, false, true);

        PaymentEventResponse response = eventService.receivePaymentEvent(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        verify(webhookClient, times(3)).invoke(eq(WEBHOOK_URL), any(WebhookPayload.class));

        List<EventDelivery> deliveries = capturedDeliveries();
        assertThat(deliveries).hasSize(3);
        assertThat(deliveries.get(0).getRetryNo()).isEqualTo(1);
        assertThat(deliveries.get(0).getDeliveryStatus()).isEqualTo(DeliveryStatus.RETRYING);
        assertThat(deliveries.get(1).getRetryNo()).isEqualTo(2);
        assertThat(deliveries.get(1).getDeliveryStatus()).isEqualTo(DeliveryStatus.RETRYING);
        assertThat(deliveries.get(2).getRetryNo()).isEqualTo(3);
        assertThat(deliveries.get(2).getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void marksFailedAfterAllRetriesExhausted() {
        stubNewEventAndTenant();
        when(webhookClient.invoke(eq(WEBHOOK_URL), any(WebhookPayload.class))).thenReturn(false);

        PaymentEventResponse response = eventService.receivePaymentEvent(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.FAILURE);
        // initial attempt + 2 retries
        verify(webhookClient, times(3)).invoke(eq(WEBHOOK_URL), any(WebhookPayload.class));

        List<EventDelivery> deliveries = capturedDeliveries();
        assertThat(deliveries).hasSize(3);
        assertThat(deliveries.get(0).getDeliveryStatus()).isEqualTo(DeliveryStatus.RETRYING);
        assertThat(deliveries.get(1).getDeliveryStatus()).isEqualTo(DeliveryStatus.RETRYING);
        assertThat(deliveries.get(2).getRetryNo()).isEqualTo(3);
        assertThat(deliveries.get(2).getDeliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    @Test
    void stopsRetryingImmediatelyOnSuccess() {
        stubNewEventAndTenant();
        when(webhookClient.invoke(eq(WEBHOOK_URL), any(WebhookPayload.class)))
                .thenReturn(false, true);

        PaymentEventResponse response = eventService.receivePaymentEvent(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        verify(webhookClient, times(2)).invoke(eq(WEBHOOK_URL), any(WebhookPayload.class));

        List<EventDelivery> deliveries = capturedDeliveries();
        assertThat(deliveries).hasSize(2);
        assertThat(deliveries.get(0).getDeliveryStatus()).isEqualTo(DeliveryStatus.RETRYING);
        assertThat(deliveries.get(1).getDeliveryStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void returnsFailureWhenTenantNotFound() {
        when(eventRepository.existsByPaymentId("pay-100")).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setEventId(10L);
            return event;
        });
        when(tenantRepository.findByMerchant(MERCHANT)).thenReturn(Optional.empty());

        PaymentEventResponse response = eventService.receivePaymentEvent(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.FAILURE);
        verify(webhookClient, never()).invoke(any(), any());
        verify(eventDeliveryRepository, never()).save(any());
    }

    @Test
    void isIdempotentForDuplicatePaymentId() {
        when(eventRepository.existsByPaymentId("pay-100")).thenReturn(true);

        PaymentEventResponse response = eventService.receivePaymentEvent(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        verify(eventRepository, never()).save(any(Event.class));
        verify(webhookClient, never()).invoke(any(), any());
        verify(eventDeliveryRepository, never()).save(any());
    }

    @Test
    void withZeroRetriesFailsOnFirstAttemptWithoutRetrying() {
        tenant.setRetriesNo(0);
        stubNewEventAndTenant();
        when(webhookClient.invoke(eq(WEBHOOK_URL), any(WebhookPayload.class))).thenReturn(false);

        PaymentEventResponse response = eventService.receivePaymentEvent(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.FAILURE);
        verify(webhookClient, times(1)).invoke(eq(WEBHOOK_URL), any(WebhookPayload.class));

        List<EventDelivery> deliveries = capturedDeliveries();
        assertThat(deliveries).hasSize(1);
        assertThat(deliveries.get(0).getDeliveryStatus()).isEqualTo(DeliveryStatus.FAILED);
    }

    private void stubNewEventAndTenant() {
        when(eventRepository.existsByPaymentId("pay-100")).thenReturn(false);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setEventId(10L);
            return event;
        });
        when(tenantRepository.findByMerchant(MERCHANT)).thenReturn(Optional.of(tenant));
        when(eventDeliveryRepository.save(any(EventDelivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private List<EventDelivery> capturedDeliveries() {
        ArgumentCaptor<EventDelivery> captor = ArgumentCaptor.forClass(EventDelivery.class);
        verify(eventDeliveryRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues();
    }
}
