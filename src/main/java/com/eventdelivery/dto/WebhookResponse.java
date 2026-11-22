package com.eventdelivery.dto;

public class WebhookResponse {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }
}
