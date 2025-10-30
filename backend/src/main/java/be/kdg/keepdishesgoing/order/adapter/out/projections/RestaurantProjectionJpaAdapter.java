package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.port.out.LoadRestaurantProjectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
        return repository.findById(restaurantId)
            .map(RestaurantProjectionJpaEntity::isOpen)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + restaurantId));
    }

    @Override
    public String getRestaurantName(UUID restaurantId) {
        return repository.findById(restaurantId)
            .map(RestaurantProjectionJpaEntity::getRestaurantName)
            .orElseThrow(() -> new IllegalArgumentException("Restaurant not found: " + restaurantId));
    }
}