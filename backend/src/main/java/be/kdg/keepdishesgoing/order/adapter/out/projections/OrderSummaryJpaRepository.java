package be.kdg.keepdishesgoing.order.adapter.out.projections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
