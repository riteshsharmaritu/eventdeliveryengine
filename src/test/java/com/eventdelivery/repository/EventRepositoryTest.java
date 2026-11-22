package com.eventdelivery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.eventdelivery.domain.Event;
import com.eventdelivery.domain.EventType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void saveAndFindByPaymentId() {
        Event event = new Event();
        event.setPaymentId("pay-100");
        event.setAmount(new BigDecimal("49.99"));
        event.setDate(Instant.parse("2026-01-15T12:00:00Z"));
        event.setInvoiceId("inv-1");
        event.setMerchant("acme");
        event.setEventType(EventType.FULLY_PAID);

        Event saved = eventRepository.save(event);

        assertThat(saved.getEventId()).isNotNull();
        assertThat(eventRepository.existsByPaymentId("pay-100")).isTrue();

        Optional<Event> found = eventRepository.findByPaymentId("pay-100");
        assertThat(found).isPresent();
        assertThat(found.get().getEventType()).isEqualTo(EventType.FULLY_PAID);
        assertThat(found.get().getAmount()).isEqualByComparingTo("49.99");
    }

    @Test
    void existsByPaymentIdReturnsFalseWhenMissing() {
        assertThat(eventRepository.existsByPaymentId("missing")).isFalse();
    }
}
