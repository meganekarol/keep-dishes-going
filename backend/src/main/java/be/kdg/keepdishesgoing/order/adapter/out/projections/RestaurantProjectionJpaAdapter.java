package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.port.out.LoadRestaurantProjectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Adapter for loading restaurant projections (read models)
 */
@Component
public class RestaurantProjectionJpaAdapter implements LoadRestaurantProjectionPort {
    
    private static final Logger log = LoggerFactory.getLogger(RestaurantProjectionJpaAdapter.class);
    
    private final RestaurantProjectionJpaRepository repository;
    
    public RestaurantProjectionJpaAdapter(RestaurantProjectionJpaRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public boolean isRestaurantOpen(UUID restaurantId) {
        // Evaluated against the clock on every call, so checkout sees the state
        // as it is at that exact moment rather than a stale stored flag.
        return repository.findById(restaurantId)
            .map(restaurant -> restaurant.isOpenAt(LocalDateTime.now()))
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + restaurantId));
    }

    @Override
    public String getRestaurantName(UUID restaurantId) {
        return repository.findById(restaurantId)
            .map(RestaurantProjectionJpaEntity::getRestaurantName)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + restaurantId));
    }

    @Override
    public int getPreparationTime(UUID restaurantId) {
        return repository.findById(restaurantId)
            .map(RestaurantProjectionJpaEntity::getDefaultPreparationTime)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + restaurantId));
    }
}