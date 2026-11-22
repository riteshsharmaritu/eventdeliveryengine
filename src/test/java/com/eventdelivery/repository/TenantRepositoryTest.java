package com.eventdelivery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.eventdelivery.domain.Tenant;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class TenantRepositoryTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Test
    void saveAndFindByMerchant() {
        Tenant tenant = new Tenant();
        tenant.setUrl("http://tenant.example/webhook");
        tenant.setMerchant("acme");
        tenant.setRetriesNo(3);
        tenant.setDateAdded(Instant.parse("2026-01-15T10:00:00Z"));

        Tenant saved = tenantRepository.save(tenant);

        assertThat(saved.getTenantId()).isNotNull();

        Optional<Tenant> found = tenantRepository.findByMerchant("acme");
        assertThat(found).isPresent();
        assertThat(found.get().getUrl()).isEqualTo("http://tenant.example/webhook");
        assertThat(found.get().getRetriesNo()).isEqualTo(3);
    }

    @Test
    void findByMerchantReturnsEmptyWhenMissing() {
        assertThat(tenantRepository.findByMerchant("unknown")).isEmpty();
    }
}
