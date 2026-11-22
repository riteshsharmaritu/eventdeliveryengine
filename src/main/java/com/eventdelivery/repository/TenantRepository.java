package com.eventdelivery.repository;

import com.eventdelivery.domain.Tenant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByMerchant(String merchant);
}
