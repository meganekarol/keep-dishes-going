package be.kdg.keepdishesgoing.order.adapter.out.persistence;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.OrderSnapshot;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Adapter for loading and saving orders using event sourcing
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
    }
    
    @Override
    public Optional<Order> loadBy(OrderId orderId) {
        log.debug("Loading order from event store: {}", orderId);
        
        try {
            // Try to load from snapshot first
            Optional<OrderSnapshotJpaEntity> snapshotEntity = 
                snapshotRepository.findById(orderId.uuid());
            
            if (snapshotEntity.isPresent()) {
                log.debug("Loading order from snapshot");
                OrderSnapshot snapshot = deserializeSnapshot(snapshotEntity.get().getSnapshotData());
                
                // Load events after snapshot
                List<OrderEventJpaEntity> eventsAfterSnapshot = 
                    eventRepository.findByOrderIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
                        orderId.uuid(), snapshot.version());
                
                if (eventsAfterSnapshot.isEmpty()) {
                    return Optional.of(Order.fromSnapshot(snapshot));
                }
                
                // TODO: Apply events after snapshot (requires event replay logic)
                // For now, just return snapshot
                return Optional.of(Order.fromSnapshot(snapshot));
            }
            
            // No snapshot, load all events
            List<OrderEventJpaEntity> events = 
                eventRepository.findByOrderIdOrderBySequenceNumberAsc(orderId.uuid());
            
            if (events.isEmpty()) {
                return Optional.empty();
            }
            
            // TODO: Rebuild order from events (requires event replay logic)
            // For this implementation, we'll primarily rely on snapshots
            log.warn("Order {} has no snapshot, event replay not fully implemented", orderId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error loading order {}", orderId, e);
            throw new RuntimeException("Failed to load order", e);
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
        
        // Get current sequence number
        long currentSequence = eventRepository.countByOrderId(order.getOrderId().uuid());
        
        // Save each event
        for (DomainEvent event : events) {
            try {
                String eventData = serializeEvent(event);
                OrderEventJpaEntity eventEntity = new OrderEventJpaEntity(
                    order.getOrderId().uuid(),
                    event.getClass().getSimpleName(),
                    eventData,
                    LocalDateTime.now(),
                    (int) currentSequence++
                );
                eventRepository.save(eventEntity);
                log.debug("Saved event {} for order {}", event.getClass().getSimpleName(), order.getOrderId());
            } catch (JsonProcessingException e) {
                log.error("Error serializing event", e);
                throw new RuntimeException("Failed to save event", e);
            }
        }
        
        // Check if we should create a snapshot
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
            
            Optional<OrderSnapshotJpaEntity> existing = 
                snapshotRepository.findById(order.getOrderId().uuid());
            
            if (existing.isPresent()) {
                // Update existing snapshot
                OrderSnapshotJpaEntity entity = existing.get();
                entity.setSnapshotData(snapshotData);
                entity.setVersion(order.getVersion());
                entity.setCreatedAt(LocalDateTime.now());
                snapshotRepository.save(entity);
            } else {
                // Create new snapshot
                OrderSnapshotJpaEntity entity = new OrderSnapshotJpaEntity(
                    order.getOrderId().uuid(),
                    snapshotData,
                    order.getVersion()
                );
                snapshotRepository.save(entity);
            }
            
            log.info("Snapshot saved for order {}", order.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("Error serializing snapshot for order {}", order.getOrderId(), e);
            throw new RuntimeException("Failed to save snapshot", e);
        }
    }
    
    private String serializeEvent(DomainEvent event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
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