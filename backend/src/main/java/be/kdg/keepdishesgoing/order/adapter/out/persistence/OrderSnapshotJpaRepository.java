package be.kdg.keepdishesgoing.order.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * JPA Repository for order snapshots
 */
@Repository
public interface OrderSnapshotJpaRepository extends JpaRepository<OrderSnapshotJpaEntity, UUID> {

}