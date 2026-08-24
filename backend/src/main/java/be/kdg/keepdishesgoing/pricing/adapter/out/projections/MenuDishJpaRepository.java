package be.kdg.keepdishesgoing.pricing.adapter.out.projections;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuDishJpaRepository extends JpaRepository<MenuDishJpaEntity, UUID> {

    List<MenuDishJpaEntity> findByRestaurantId(UUID restaurantId);
}
