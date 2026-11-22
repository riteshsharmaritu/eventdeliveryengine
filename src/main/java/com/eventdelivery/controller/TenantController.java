package com.eventdelivery.controller;

import com.eventdelivery.dto.RegisterTenantRequest;
import com.eventdelivery.dto.RegisterTenantResponse;
import com.eventdelivery.service.TenantService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class TenantController {

    private static final Logger log = LoggerFactory.getLogger(TenantController.class);

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterTenantResponse> register(
            @Valid @RequestBody RegisterTenantRequest request) {
        log.info("POST /v1/register merchant={} retries_no={}",
                request.getMerchantName(), request.getRetriesNo());
        RegisterTenantResponse response = tenantService.register(request);
        log.info("POST /v1/register completed status={}", response.getStatus());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
