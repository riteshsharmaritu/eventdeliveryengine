package com.eventdelivery.repository;

import com.eventdelivery.domain.EventDelivery;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventDeliveryRepository extends JpaRepository<EventDelivery, Long> {

    List<EventDelivery> findByEventEventId(Long eventId);
}
