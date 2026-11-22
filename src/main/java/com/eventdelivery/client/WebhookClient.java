package com.eventdelivery.client;

import com.eventdelivery.dto.WebhookPayload;

public interface WebhookClient {

    /**
     * Invokes the tenant webhook. Returns true when the tenant reports success.
     */
    boolean invoke(String url, WebhookPayload payload);
}
