package com.eventdelivery.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.eventdelivery.domain.DeliveryStatus;
import com.eventdelivery.domain.Event;
import com.eventdelivery.domain.EventDelivery;
import com.eventdelivery.domain.EventType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class EventDeliveryRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventDeliveryRepository eventDeliveryRepository;

    @Test
    void saveAndFindByEventId() {
        Event event = new Event();
        event.setPaymentId("pay-200");
        event.setAmount(new BigDecimal("10.00"));
        event.setDate(Instant.parse("2026-01-15T13:00:00Z"));
        event.setInvoiceId("inv-2");
        event.setMerchant("acme");
        event.setEventType(EventType.PAYMENT_FAILED);
        Event savedEvent = eventRepository.save(event);

        EventDelivery delivery = new EventDelivery();
        delivery.setEvent(savedEvent);
        delivery.setRetryNo(1);
        delivery.setDeliveryStatus(DeliveryStatus.RETRYING);
        delivery.setDateAdded(Instant.parse("2026-01-15T13:01:00Z"));

        eventDeliveryRepository.save(delivery);

        List<EventDelivery> deliveries =
                eventDeliveryRepository.findByEventEventId(savedEvent.getEventId());

        assertThat(deliveries).hasSize(1);
        assertThat(deliveries.get(0).getRetryNo()).isEqualTo(1);
        assertThat(deliveries.get(0).getDeliveryStatus()).isEqualTo(DeliveryStatus.RETRYING);
    }
}
