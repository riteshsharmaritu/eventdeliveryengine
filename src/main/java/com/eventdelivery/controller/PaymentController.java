package com.eventdelivery.controller;

import com.eventdelivery.domain.ResponseStatus;
import com.eventdelivery.dto.PaymentEventRequest;
import com.eventdelivery.dto.PaymentEventResponse;
import com.eventdelivery.service.EventService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final EventService eventService;

    public PaymentController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentEventResponse> receivePayment(
            @Valid @RequestBody PaymentEventRequest request) {
        log.info("POST /v1/payment payment_id={} merchant={} event_type={}",
                request.getPaymentId(), request.getMerchant(), request.getEventType());
        PaymentEventResponse response = eventService.receivePaymentEvent(request);
        HttpStatus status = response.getStatus() == ResponseStatus.SUCCESS
                ? HttpStatus.CREATED
                : HttpStatus.UNPROCESSABLE_ENTITY;
        log.info("POST /v1/payment completed payment_id={} status={} http={}",
                request.getPaymentId(), response.getStatus(), status.value());
        return ResponseEntity.status(status).body(response);
    }
}
