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
        try {
            WebhookResponse response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(WebhookResponse.class);

            return response != null && response.isSuccess();
        } catch (RestClientException ex) {
            log.warn("Webhook invocation failed for url={}: {}", url, ex.getMessage());
            return false;
        }
    }
}
