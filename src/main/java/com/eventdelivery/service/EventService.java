package com.eventdelivery.service;

import com.eventdelivery.domain.Event;
import com.eventdelivery.domain.ResponseStatus;
import com.eventdelivery.dto.PaymentEventRequest;
import com.eventdelivery.dto.PaymentEventResponse;
import com.eventdelivery.repository.EventRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public PaymentEventResponse receivePaymentEvent(PaymentEventRequest request) {
        Event event = new Event();
        event.setPaymentId(request.getPaymentId());
        event.setAmount(request.getAmount());
        event.setDate(request.getDate());
        event.setInvoiceId(request.getInvoiceId());
        event.setMerchant(request.getMerchant());
        event.setEventType(request.getEventType());

        eventRepository.save(event);

        return new PaymentEventResponse(ResponseStatus.SUCCESS, Instant.now());
    }
}
