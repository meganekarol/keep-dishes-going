package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.port.in.GetOwnerRestaurantQuery;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetOwnerRestaurantQueryImpl implements GetOwnerRestaurantQuery {

    private final LoadRestaurantPort loadRestaurantPort;

    public GetOwnerRestaurantQueryImpl(LoadRestaurantPort loadRestaurantPort) {
        this.loadRestaurantPort = loadRestaurantPort;
    }

    @Override
    public Optional<Restaurant> getOwnerRestaurant(UUID ownerId) {
        return loadRestaurantPort.loadByOwnerId(OwnerId.of(ownerId));
    }
}
