package be.kdg.keepdishesgoing.pricing.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuPricePointJpaRepository extends JpaRepository<MenuPricePointJpaEntity, UUID> {

    List<MenuPricePointJpaEntity> findByRestaurantIdOrderByRecordedAtAsc(UUID restaurantId);
}
