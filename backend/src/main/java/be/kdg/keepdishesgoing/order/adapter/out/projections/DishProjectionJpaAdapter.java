package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.port.out.LoadDishProjectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * JPA Adapter for loading dish projections (read models)
 */
@Component
public class DishProjectionJpaAdapter implements LoadDishProjectionPort {
    
    private static final Logger log = LoggerFactory.getLogger(DishProjectionJpaAdapter.class);
    
    private final DishProjectionJpaRepository repository;
    
    public DishProjectionJpaAdapter(DishProjectionJpaRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public boolean isDishAvailable(UUID dishId) {
        return repository.findById(dishId)
            .map(dish -> dish.getStatus() == DishProjectionStatus.AVAILABLE)
            .orElse(false);
    }
    
    @Override
    public boolean areAllDishesAvailable(List<UUID> dishIds) {
        for (UUID dishId : dishIds) {
            if (!isDishAvailable(dishId)) {
                log.warn("Dish {} is not available", dishId);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public BigDecimal getDishPrice(UUID dishId) {
        return repository.findById(dishId)
            .map(DishProjectionJpaEntity::getPrice)
            .orElseThrow(() -> new IllegalArgumentException("Dish not found: " + dishId));
    }
    
    @Override
    public String getDishName(UUID dishId) {
        return repository.findById(dishId)
            .map(DishProjectionJpaEntity::getDishName)
            .orElseThrow(() -> new IllegalArgumentException("Dish not found: " + dishId));
    }
    
    @Override
    public boolean dishBelongsToRestaurant(UUID dishId, UUID restaurantId) {
        return repository.findById(dishId)
            .map(dish -> dish.getRestaurantId().equals(restaurantId))
            .orElse(false);
    }
}