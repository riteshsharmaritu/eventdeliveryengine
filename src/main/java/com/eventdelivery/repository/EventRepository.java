package com.eventdelivery.repository;

import com.eventdelivery.domain.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByPaymentId(String paymentId);

    boolean existsByPaymentId(String paymentId);
}
