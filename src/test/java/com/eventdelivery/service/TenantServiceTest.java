package com.eventdelivery.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eventdelivery.domain.ResponseStatus;
import com.eventdelivery.domain.Tenant;
import com.eventdelivery.dto.RegisterTenantRequest;
import com.eventdelivery.dto.RegisterTenantResponse;
import com.eventdelivery.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void registerPersistsTenantAndReturnsSuccess() {
        RegisterTenantRequest request = new RegisterTenantRequest();
        request.setUrl("http://tenant.example/webhook");
        request.setMerchantName("acme");
        request.setRetriesNo(5);

        when(tenantRepository.save(any(Tenant.class))).thenAnswer(invocation -> {
            Tenant tenant = invocation.getArgument(0);
            tenant.setTenantId(1L);
            return tenant;
        });

        RegisterTenantResponse response = tenantService.register(request);

        assertThat(response.getStatus()).isEqualTo(ResponseStatus.SUCCESS);
        assertThat(response.getDate()).isNotNull();

        ArgumentCaptor<Tenant> captor = ArgumentCaptor.forClass(Tenant.class);
        verify(tenantRepository).save(captor.capture());
        assertThat(captor.getValue().getMerchant()).isEqualTo("acme");
        assertThat(captor.getValue().getUrl()).isEqualTo("http://tenant.example/webhook");
        assertThat(captor.getValue().getRetriesNo()).isEqualTo(5);
        assertThat(captor.getValue().getDateAdded()).isNotNull();
    }
}
