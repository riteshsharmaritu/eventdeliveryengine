package com.eventdelivery.controller;

import com.eventdelivery.domain.ResponseStatus;
import com.eventdelivery.dto.PaymentEventRequest;
import com.eventdelivery.dto.PaymentEventResponse;
import com.eventdelivery.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class PaymentController {

    private final EventService eventService;

    public PaymentController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/payment")
    public ResponseEntity<PaymentEventResponse> receivePayment(
            @Valid @RequestBody PaymentEventRequest request) {
        PaymentEventResponse response = eventService.receivePaymentEvent(request);
        HttpStatus status = response.getStatus() == ResponseStatus.SUCCESS
                ? HttpStatus.CREATED
                : HttpStatus.UNPROCESSABLE_ENTITY;
        return ResponseEntity.status(status).body(response);
    }
}
