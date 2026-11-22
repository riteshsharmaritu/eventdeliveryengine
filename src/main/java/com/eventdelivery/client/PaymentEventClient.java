package com.eventdelivery.client;

import com.eventdelivery.dto.PaymentEventRequest;
import com.eventdelivery.dto.PaymentEventResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Thin client for invoking the /v1/payment API.
 */
@Component
public class PaymentEventClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventClient.class);

    private final RestClient restClient;
    private final String baseUrl;

    public PaymentEventClient(
            RestClient.Builder restClientBuilder,
            @Value("${eventdelivery.base-url:http://localhost:8080}") String baseUrl) {
        this.restClient = restClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    public PaymentEventResponse sendPaymentEvent(PaymentEventRequest request) {
        String uri = baseUrl + "/v1/payment";
        log.info("Sending payment event to {} payment_id={} merchant={}",
                uri, request.getPaymentId(), request.getMerchant());
        PaymentEventResponse response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PaymentEventResponse.class);
        log.info("Payment event client response payment_id={} status={}",
                request.getPaymentId(),
                response != null ? response.getStatus() : null);
        return response;
    }
}
