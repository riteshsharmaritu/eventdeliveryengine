package com.eventdelivery.client;

import com.eventdelivery.dto.WebhookPayload;
import com.eventdelivery.dto.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class RestWebhookClient implements WebhookClient {

    private static final Logger log = LoggerFactory.getLogger(RestWebhookClient.class);

    private final RestClient restClient;

    public RestWebhookClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public boolean invoke(String url, WebhookPayload payload) {
        log.info("Calling webhook url={} payment_id={}", url, payload.getPaymentId());
        try {
            WebhookResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(WebhookResponse.class);

            boolean success = response != null && response.isSuccess();
            log.info("Webhook response url={} payment_id={} success={}",
                    url, payload.getPaymentId(), success);
            return success;
        } catch (RestClientException ex) {
            log.warn("Webhook call failed url={} payment_id={} reason={}",
                    url, payload.getPaymentId(), ex.getMessage());
            return false;
        }
    }
}
