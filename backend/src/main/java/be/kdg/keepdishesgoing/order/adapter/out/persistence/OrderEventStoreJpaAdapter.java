package be.kdg.keepdishesgoing.order.adapter.out.persistence;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.common.events.*;
import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.OrderSnapshot;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Adapter for loading and saving orders using event sourcing.
 *
 * <p>Loading rehydrates the aggregate by starting from a snapshot (if one exists)
 * or from a blank aggregate, then replaying every event that post-dates that
 * starting point. Saving appends the aggregate's uncommitted events and creates a
 * snapshot every {@link #SNAPSHOT_FREQUENCY} events.
 */
@Component
public class OrderEventStoreJpaAdapter implements LoadOrderPort, SaveOrderPort {

    private static final Logger log = LoggerFactory.getLogger(OrderEventStoreJpaAdapter.class);
    private static final int SNAPSHOT_FREQUENCY = 50; // Create snapshot every 50 events

    private final OrderEventJpaRepository eventRepository;
    private final OrderSnapshotJpaRepository snapshotRepository;
    private final ObjectMapper objectMapper;

    public OrderEventStoreJpaAdapter(
            OrderEventJpaRepository eventRepository,
            OrderSnapshotJpaRepository snapshotRepository) {
        this.eventRepository = eventRepository;
        this.snapshotRepository = snapshotRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        // An event store must read what it wrote years ago, so it reads
        // leniently: a property the current class no longer declares is skipped
        // rather than thrown on.
        //
        // It is also load-bearing today. Jackson sees DeliveryAddress's derived
        // getFullAddress() as a property and writes it into every snapshot, but
        // the record's constructor has no such component to read it back into.
        // Marking up the value object with a Jackson annotation would drag
        // serialisation concerns into the domain; keeping the leniency here
        // leaves the domain technology-agnostic and repairs snapshots already
        // written.
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Optional<Order> loadBy(OrderId orderId) {
        log.debug("Loading order from event store: {}", orderId);
        UUID id = orderId.uuid();

        Optional<OrderSnapshotJpaEntity> snapshotEntity = snapshotRepository.findById(id);

        if (snapshotEntity.isPresent()) {
            OrderSnapshot snapshot = deserializeSnapshot(snapshotEntity.get().getSnapshotData());
            Order order = Order.fromSnapshot(snapshot);

            int lastSequence = snapshotEntity.get().getLastSequenceNumber();
            List<OrderEventJpaEntity> eventsAfterSnapshot =
                    eventRepository.findByOrderIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(id, lastSequence);

            log.debug("Loaded order {} from snapshot (seq {}), replaying {} later events",
                    orderId, lastSequence, eventsAfterSnapshot.size());
            replayInto(order, eventsAfterSnapshot);
            return Optional.of(order);
        }

        // No snapshot: rebuild from a blank aggregate by replaying all events
        List<OrderEventJpaEntity> events = eventRepository.findByOrderIdOrderBySequenceNumberAsc(id);
        if (events.isEmpty()) {
            return Optional.empty();
        }

        log.debug("Loaded order {} by replaying {} events from blank aggregate", orderId, events.size());
        Order order = Order.blank();
        replayInto(order, events);
        return Optional.of(order);
    }

    private void replayInto(Order order, List<OrderEventJpaEntity> events) {
        for (OrderEventJpaEntity entity : events) {
            order.apply(deserializeEvent(entity));
        }
    }

    @Override
    public void save(Order order) {
        log.debug("Saving order events: {}", order.getOrderId());

        List<DomainEvent> events = order.getUncommittedEvents();
        if (events.isEmpty()) {
            log.debug("No uncommitted events to save for order {}", order.getOrderId());
            return;
        }

        // Get current sequence number (0-based, monotonic per order)
        long currentSequence = eventRepository.countByOrderId(order.getOrderId().uuid());

        for (DomainEvent event : events) {
            try {
                String eventData = serializeEvent(event);
                OrderEventJpaEntity eventEntity = new OrderEventJpaEntity(
                        order.getOrderId().uuid(),
                        event.getClass().getSimpleName(),
                        eventData,
                        event.occurredAt() != null ? event.occurredAt() : LocalDateTime.now(),
                        (int) currentSequence++
                );
                eventRepository.save(eventEntity);
                log.debug("Saved event {} for order {}", event.getClass().getSimpleName(), order.getOrderId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing event", e);
                throw new RuntimeException("Failed to save event", e);
            }
        }

        // Create a snapshot every SNAPSHOT_FREQUENCY events
        if (currentSequence % SNAPSHOT_FREQUENCY == 0) {
            log.info("Creating automatic snapshot for order {} at sequence {}",
                    order.getOrderId(), currentSequence);
            saveSnapshot(order);
        }
    }

    @Override
    public void saveSnapshot(Order order) {
        log.debug("Saving snapshot for order: {}", order.getOrderId());

        try {
            OrderSnapshot snapshot = order.createSnapshot();
            String snapshotData = serializeSnapshot(snapshot);
            int lastSequence = (int) eventRepository.countByOrderId(order.getOrderId().uuid()) - 1;

            Optional<OrderSnapshotJpaEntity> existing =
                    snapshotRepository.findById(order.getOrderId().uuid());

            if (existing.isPresent()) {
                OrderSnapshotJpaEntity entity = existing.get();
                entity.setSnapshotData(snapshotData);
                entity.setVersion(order.getVersion());
                entity.setLastSequenceNumber(lastSequence);
                entity.setCreatedAt(LocalDateTime.now());
                snapshotRepository.save(entity);
            } else {
                OrderSnapshotJpaEntity entity = new OrderSnapshotJpaEntity(
                        order.getOrderId().uuid(),
                        snapshotData,
                        order.getVersion(),
                        lastSequence
                );
                snapshotRepository.save(entity);
            }

            log.info("Snapshot saved for order {} at sequence {}", order.getOrderId(), lastSequence);
        } catch (JsonProcessingException e) {
            log.error("Error serializing snapshot for order {}", order.getOrderId(), e);
            throw new RuntimeException("Failed to save snapshot", e);
        }
    }

    private String serializeEvent(DomainEvent event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    private DomainEvent deserializeEvent(OrderEventJpaEntity entity) {
        try {
            return objectMapper.readValue(entity.getEventData(), resolveEventType(entity.getEventType()));
        } catch (JsonProcessingException e) {
            log.error("Error deserializing event {} for order {}", entity.getEventType(), entity.getOrderId(), e);
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }

    private Class<? extends DomainEvent> resolveEventType(String eventType) {
        return switch (eventType) {
            case "OrderPlacedEvent" -> OrderPlacedEvent.class;
            case "OrderAcceptedEvent" -> OrderAcceptedEvent.class;
            case "OrderRejectedEvent" -> OrderRejectedEvent.class;
            case "OrderReadyEvent" -> OrderReadyEvent.class;
            case "OrderPickedUpEvent" -> OrderPickedUpEvent.class;
            case "OrderDeliveredEvent" -> OrderDeliveredEvent.class;
            default -> throw new IllegalStateException("Unknown order event type: " + eventType);
        };
    }

    private String serializeSnapshot(OrderSnapshot snapshot) throws JsonProcessingException {
        return objectMapper.writeValueAsString(snapshot);
    }

    private OrderSnapshot deserializeSnapshot(String snapshotData) {
        try {
            return objectMapper.readValue(snapshotData, OrderSnapshot.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing snapshot", e);
            throw new RuntimeException("Failed to deserialize snapshot", e);
        }
    }
}