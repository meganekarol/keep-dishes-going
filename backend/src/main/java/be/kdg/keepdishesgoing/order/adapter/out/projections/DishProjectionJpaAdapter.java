package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.port.out.LoadDishProjectionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    public List<String> namesOfUnavailableDishes(List<UUID> dishIds) {
        List<String> names = new ArrayList<>();

        for (UUID dishId : dishIds) {
            DishProjectionJpaEntity dish = repository.findById(dishId).orElse(null);
            if (dish != null && dish.getStatus() == DishProjectionStatus.AVAILABLE) {
                continue;
            }

            log.warn("Dish {} is not available", dishId);
            // A dish the projection has never seen is unavailable too, and the
            // only name there is to give is the id the caller sent.
            names.add(dish == null ? dishId.toString() : dish.getDishName());
        }

        return names;
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