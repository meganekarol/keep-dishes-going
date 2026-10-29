package be.kdg.keepdishesgoing.restaurant.adapter.out.repositories;

import be.kdg.keepdishesgoing.restaurant.adapter.out.RestaurantJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantJpaRepository extends JpaRepository<RestaurantJpaEntity,UUID> {
   Optional<RestaurantJpaEntity> findByOwnerId (UUID owner);
}
