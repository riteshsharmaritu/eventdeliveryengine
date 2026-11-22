package com.eventdelivery.controller;

import com.eventdelivery.dto.WebhookPayload;
import com.eventdelivery.dto.WebhookResponse;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stand-in tenant webhook that randomly returns success or failure.
 */
@RestController
@RequestMapping("/mock-tenant")
public class MockTenantWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MockTenantWebhookController.class);

    @PostMapping("/webhook")
    public WebhookResponse receive(@RequestBody WebhookPayload payload) {
        boolean success = ThreadLocalRandom.current().nextBoolean();
        WebhookResponse response = new WebhookResponse();
        response.setStatus(success ? "success" : "failure");
        log.info(
                "Mock tenant webhook for payment_id={} -> {}",
                payload.getPaymentId(),
                response.getStatus());
        return response;
    }
}
