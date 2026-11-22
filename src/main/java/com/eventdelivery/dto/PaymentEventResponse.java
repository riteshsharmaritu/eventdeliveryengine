package com.eventdelivery.dto;

import com.eventdelivery.domain.ResponseStatus;
import java.time.Instant;

public class PaymentEventResponse {

    private ResponseStatus status;
    private Instant date;

    public PaymentEventResponse() {
    }

    public PaymentEventResponse(ResponseStatus status, Instant date) {
        this.status = status;
        this.date = date;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }
}
