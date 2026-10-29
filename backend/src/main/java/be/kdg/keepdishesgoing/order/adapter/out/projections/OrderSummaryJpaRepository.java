package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
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
     *
     * <p>Unpaid checkouts are left out. An order nobody has paid for is not the
     * kitchen's business, and showing one would invite an owner to start cooking
     * against money that may never arrive (US-23).
     */
    List<OrderSummaryJpaEntity> findByRestaurantIdAndStatusInOrderByPlacedAtDesc(
            UUID restaurantId, Collection<OrderStatus> statuses);

    /**
     * Ids of the orders a restaurant has not answered yet, in front of it since
     * before the cut-off. Only the ids are selected: the rows themselves are of
     * no use to the auto-rejection run, which reloads each aggregate.
     */
    @Query("""
            SELECT summary.orderId FROM OrderSummaryJpaEntity summary
            WHERE summary.status = :status AND summary.pendingSince < :cutoff
            """)
    List<UUID> findIdsByStatusPendingSinceBefore(@Param("status") OrderStatus status,
                                                 @Param("cutoff") LocalDateTime cutoff);

    /**
     * Ids of checkouts started before the cut-off that were never paid for.
     * Counted from {@code placedAt}, which for these rows is when the customer
     * was sent off to the payment provider (US-23).
     */
    @Query("""
            SELECT summary.orderId FROM OrderSummaryJpaEntity summary
            WHERE summary.status = :status AND summary.placedAt < :cutoff
            """)
    List<UUID> findIdsByStatusPlacedBefore(@Param("status") OrderStatus status,
                                           @Param("cutoff") LocalDateTime cutoff);

    /**
     * Orders still occupying a kitchen: taken on but not yet handed over.
     * Everything past ready for pickup belongs to the courier, not the cooks.
     */
    long countByRestaurantIdAndStatusIn(UUID restaurantId, Collection<OrderStatus> statuses);
}
