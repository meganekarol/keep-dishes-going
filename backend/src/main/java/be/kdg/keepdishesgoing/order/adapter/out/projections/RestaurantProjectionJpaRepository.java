package be.kdg.keepdishesgoing.order.adapter.out.projections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for restaurant projections in the Order context.
 * Used to query restaurant information and availability.
 */
@Repository
public interface RestaurantProjectionJpaRepository extends JpaRepository<RestaurantProjectionJpaEntity, UUID> {
}