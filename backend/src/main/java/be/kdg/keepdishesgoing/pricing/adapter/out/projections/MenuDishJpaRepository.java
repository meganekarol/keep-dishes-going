package be.kdg.keepdishesgoing.pricing.adapter.out.projections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MenuDishJpaRepository extends JpaRepository<MenuDishJpaEntity, UUID> {

    List<MenuDishJpaEntity> findByRestaurantId(UUID restaurantId);

    /** Every restaurant that has put something on its menu. */
    @Query("select distinct d.restaurantId from MenuDishJpaEntity d")
    List<UUID> findDistinctRestaurantIds();
}
