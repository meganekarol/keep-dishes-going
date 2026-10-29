package be.kdg.keepdishesgoing.restaurant.adapter.out.projections;

import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantBusynessPort;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantBusynessJpaAdapter implements LoadRestaurantBusynessPort {

    private final RestaurantBusynessJpaRepository repository;

    public RestaurantBusynessJpaAdapter(RestaurantBusynessJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public int ordersNotReadyFor(UUID restaurantId) {
        return repository.findById(restaurantId)
                .map(RestaurantBusynessJpaEntity::getOrdersNotReady)
                .orElse(0);
    }

    @Override
    public Map<UUID, Integer> ordersNotReadyByRestaurant() {
        return repository.findAll().stream()
                .collect(Collectors.toMap(
                        RestaurantBusynessJpaEntity::getRestaurantId,
                        RestaurantBusynessJpaEntity::getOrdersNotReady));
    }
}
