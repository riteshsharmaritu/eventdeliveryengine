package com.eventdelivery.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterTenantRequest {

    @NotBlank
    private String url;

    @NotNull
    @Min(0)
    @JsonProperty("retries_no")
    private Integer retriesNo;

    @NotBlank
    @JsonProperty("merchant_name")
    private String merchantName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getRetriesNo() {
        return retriesNo;
    }

    public void setRetriesNo(Integer retriesNo) {
        this.retriesNo = retriesNo;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }
}
