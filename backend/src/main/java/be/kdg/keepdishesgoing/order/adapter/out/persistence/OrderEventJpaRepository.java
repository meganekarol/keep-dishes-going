package be.kdg.keepdishesgoing.order.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for order events
 */
@Repository
public interface OrderEventJpaRepository extends JpaRepository<OrderEventJpaEntity, UUID> {
    
    /**
     * Find all events for an order, ordered by sequence number
     */
    List<OrderEventJpaEntity> findByOrderIdOrderBySequenceNumberAsc(UUID orderId);
    
    /**
     * Find events for an order after a specific sequence number (for loading after snapshot)
     */
    List<OrderEventJpaEntity> findByOrderIdAndSequenceNumberGreaterThanOrderBySequenceNumberAsc(
        UUID orderId, Integer sequenceNumber);
    
    /**
     * Count events for an order
     */
    long countByOrderId(UUID orderId);

    /**
     * Find all events of a given type that occurred before a cut-off time.
     * Used by the auto-rejection scheduler to locate stale placed orders.
     */
    List<OrderEventJpaEntity> findByEventTypeAndOccurredOnBefore(String eventType, LocalDateTime cutoff);
}