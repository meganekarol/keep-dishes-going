package be.kdg.keepdishesgoing.order.adapter.out.projections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for dish projections in the Order context.
 * Used to query available dishes when customers are building baskets.
 */
@Repository
public interface DishProjectionJpaRepository extends JpaRepository<DishProjectionJpaEntity, UUID> {

}