package com.eventdelivery.service;

import com.eventdelivery.domain.ResponseStatus;
import com.eventdelivery.domain.Tenant;
import com.eventdelivery.dto.RegisterTenantRequest;
import com.eventdelivery.dto.RegisterTenantResponse;
import com.eventdelivery.repository.TenantRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public RegisterTenantResponse register(RegisterTenantRequest request) {
        Instant now = Instant.now();

        Tenant tenant = new Tenant();
        tenant.setUrl(request.getUrl());
        tenant.setMerchant(request.getMerchantName());
        tenant.setRetriesNo(request.getRetriesNo());
        tenant.setDateAdded(now);

        tenantRepository.save(tenant);

        return new RegisterTenantResponse(ResponseStatus.SUCCESS, now);
    }
}
