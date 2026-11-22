package com.eventdelivery.service;

import com.eventdelivery.client.WebhookClient;
import com.eventdelivery.domain.DeliveryStatus;
import com.eventdelivery.domain.Event;
import com.eventdelivery.domain.EventDelivery;
import com.eventdelivery.domain.ResponseStatus;
import com.eventdelivery.domain.Tenant;
import com.eventdelivery.dto.PaymentEventRequest;
import com.eventdelivery.dto.PaymentEventResponse;
import com.eventdelivery.dto.WebhookPayload;
import com.eventdelivery.repository.EventDeliveryRepository;
import com.eventdelivery.repository.EventRepository;
import com.eventdelivery.repository.TenantRepository;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final TenantRepository tenantRepository;
    private final EventDeliveryRepository eventDeliveryRepository;
    private final WebhookClient webhookClient;

    public EventService(
            EventRepository eventRepository,
            TenantRepository tenantRepository,
            EventDeliveryRepository eventDeliveryRepository,
            WebhookClient webhookClient) {
        this.eventRepository = eventRepository;
        this.tenantRepository = tenantRepository;
        this.eventDeliveryRepository = eventDeliveryRepository;
        this.webhookClient = webhookClient;
    }

    @Transactional
    public PaymentEventResponse receivePaymentEvent(PaymentEventRequest request) {
        Instant now = Instant.now();

        if (eventRepository.existsByPaymentId(request.getPaymentId())) {
            log.info("Duplicate payment_id={} — skipping delivery (idempotent)",
                    request.getPaymentId());
            return new PaymentEventResponse(ResponseStatus.SUCCESS, now);
        }

        Event event = new Event();
        event.setPaymentId(request.getPaymentId());
        event.setAmount(request.getAmount());
        event.setDate(request.getDate());
        event.setInvoiceId(request.getInvoiceId());
        event.setMerchant(request.getMerchant());
        event.setEventType(request.getEventType());
        Event savedEvent = eventRepository.save(event);
        log.info("Event saved event_id={} payment_id={} merchant={}",
                savedEvent.getEventId(), savedEvent.getPaymentId(), savedEvent.getMerchant());

        Optional<Tenant> tenantOpt = tenantRepository.findByMerchant(request.getMerchant());
        if (tenantOpt.isEmpty()) {
            log.warn("No tenant found for merchant={} payment_id={}",
                    request.getMerchant(), request.getPaymentId());
            return new PaymentEventResponse(ResponseStatus.FAILURE, Instant.now());
        }

        Tenant tenant = tenantOpt.get();
        log.info("Delivering event_id={} to tenant_id={} url={} max_retries={}",
                savedEvent.getEventId(), tenant.getTenantId(), tenant.getUrl(), tenant.getRetriesNo());
        boolean delivered = deliverWithRetries(savedEvent, tenant);

        log.info("Delivery finished event_id={} payment_id={} result={}",
                savedEvent.getEventId(),
                savedEvent.getPaymentId(),
                delivered ? "SUCCESS" : "FAILURE");
        return new PaymentEventResponse(
                delivered ? ResponseStatus.SUCCESS : ResponseStatus.FAILURE,
                Instant.now());
    }

    /**
     * Attempts delivery once, then retries up to {@code tenant.retriesNo} more times on failure.
     * Every attempt is audited in {@code event_deliveries}.
     */
    private boolean deliverWithRetries(Event event, Tenant tenant) {
        int maxRetries = Math.max(0, tenant.getRetriesNo());
        int totalAttempts = maxRetries + 1;
        WebhookPayload payload = toPayload(event);

        for (int attempt = 1; attempt <= totalAttempts; attempt++) {
            log.info("Webhook attempt {}/{} event_id={} payment_id={}",
                    attempt, totalAttempts, event.getEventId(), event.getPaymentId());
            boolean success = webhookClient.invoke(tenant.getUrl(), payload);
            boolean lastAttempt = attempt == totalAttempts;

            DeliveryStatus status;
            if (success) {
                status = DeliveryStatus.DELIVERED;
            } else if (lastAttempt) {
                status = DeliveryStatus.FAILED;
            } else {
                status = DeliveryStatus.RETRYING;
            }

            recordDelivery(event, attempt, status);
            log.info("Delivery audit event_id={} retry_no={} status={}",
                    event.getEventId(), attempt, status);

            if (success) {
                return true;
            }
        }

        return false;
    }

    private void recordDelivery(Event event, int retryNo, DeliveryStatus status) {
        EventDelivery delivery = new EventDelivery();
        delivery.setEvent(event);
        delivery.setRetryNo(retryNo);
        delivery.setDeliveryStatus(status);
        delivery.setDateAdded(Instant.now());
        eventDeliveryRepository.save(delivery);
    }

    private WebhookPayload toPayload(Event event) {
        WebhookPayload payload = new WebhookPayload();
        payload.setPaymentId(event.getPaymentId());
        payload.setAmount(event.getAmount());
        payload.setDate(event.getDate());
        payload.setInvoiceId(event.getInvoiceId());
        payload.setMerchant(event.getMerchant());
        payload.setEventType(event.getEventType());
        return payload;
    }
}
