package be.kdg.keepdishesgoing.restaurant.adapter.out.repositories;

import be.kdg.keepdishesgoing.restaurant.adapter.out.RestaurantMenuJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantMenuJpaRepository extends JpaRepository<RestaurantMenuJpaEntity, UUID> {
    Optional<RestaurantMenuJpaEntity> findByRestaurantId(UUID restaurant);
}
