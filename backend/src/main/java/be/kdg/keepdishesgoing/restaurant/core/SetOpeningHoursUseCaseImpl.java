package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.SetOpeningHoursUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.SetOpeningHoursCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.UpdateRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SetOpeningHoursUseCaseImpl implements SetOpeningHoursUseCase {

    private final LoadRestaurantPort loadRestaurantPort;
    private final UpdateRestaurantPort updateRestaurantPort;

    public SetOpeningHoursUseCaseImpl(LoadRestaurantPort loadRestaurantPort, UpdateRestaurantPort updateRestaurantPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.updateRestaurantPort = updateRestaurantPort;
    }

    @Override
    public void setOpeningHours(SetOpeningHoursCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());
        Restaurant restaurant = loadRestaurantPort.loadById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        restaurant.defineOpeningHours(command.schedule());
        updateRestaurantPort.updateRestaurant(restaurant);
    }
}
