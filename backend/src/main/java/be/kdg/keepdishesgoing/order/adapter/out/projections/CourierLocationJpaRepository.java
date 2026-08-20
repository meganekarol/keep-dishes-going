package be.kdg.keepdishesgoing.order.adapter.out.projections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CourierLocationJpaRepository extends JpaRepository<CourierLocationJpaEntity, UUID> {
}
