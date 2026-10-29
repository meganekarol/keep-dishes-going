package be.kdg.keepdishesgoing.order.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
     * Every distinct order in the store, oldest first, for rebuilding the read
     * model.
     */
    @Query("""
            SELECT event.orderId FROM OrderEventJpaEntity event
            GROUP BY event.orderId
            ORDER BY MIN(event.occurredOn)
            """)
    List<UUID> findAllOrderIds();
}