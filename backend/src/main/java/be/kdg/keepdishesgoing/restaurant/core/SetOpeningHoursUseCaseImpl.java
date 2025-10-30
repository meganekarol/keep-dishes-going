package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.SetOpeningHoursUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.SetOpeningHoursCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SetOpeningHoursUseCaseImpl implements SetOpeningHoursUseCase {

    private final LoadRestaurantPort loadRestaurantPort;
    private final SaveRestaurantPort saveRestaurantPort;

    public SetOpeningHoursUseCaseImpl(LoadRestaurantPort loadRestaurantPort, SaveRestaurantPort saveRestaurantPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.saveRestaurantPort = saveRestaurantPort;
    }

    @Override
    public void setOpeningHours(SetOpeningHoursCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());
        Restaurant restaurant = loadRestaurantPort.loadById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        restaurant.defineOpeningHours(command.schedule());
        saveRestaurantPort.saveRestaurant(restaurant);
    }
}
