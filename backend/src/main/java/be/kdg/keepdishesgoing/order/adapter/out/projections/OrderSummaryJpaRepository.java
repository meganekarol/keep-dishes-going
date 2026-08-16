package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA repository for the per-restaurant order read model.
 */
@Repository
public interface OrderSummaryJpaRepository extends JpaRepository<OrderSummaryJpaEntity, UUID> {

    /**
     * Newest first: an owner works through what just came in, and older orders
     * are history they scroll to.
     */
    List<OrderSummaryJpaEntity> findByRestaurantIdOrderByPlacedAtDesc(UUID restaurantId);

    /**
     * Ids of the orders a restaurant has not answered yet, placed before the
     * cut-off. Only the ids are selected: the rows themselves are of no use to
     * the auto-rejection run, which reloads each aggregate.
     */
    @Query("""
            SELECT summary.orderId FROM OrderSummaryJpaEntity summary
            WHERE summary.status = :status AND summary.placedAt < :cutoff
            """)
    List<UUID> findIdsByStatusPlacedBefore(@Param("status") OrderStatus status,
                                           @Param("cutoff") LocalDateTime cutoff);
}
