package com.eventdelivery.client;

import com.eventdelivery.dto.PaymentEventRequest;
import com.eventdelivery.dto.PaymentEventResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin client for invoking the /v1/payment API.
 */
@Component
public class PaymentEventClient {

    private final RestClient restClient;
    private final String baseUrl;

    public PaymentEventClient(
            RestClient.Builder restClientBuilder,
            @Value("${eventdelivery.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    public PaymentEventResponse sendPaymentEvent(PaymentEventRequest request) {
        return restClient.post()
                .uri(baseUrl + "/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PaymentEventResponse.class);
    }
}
