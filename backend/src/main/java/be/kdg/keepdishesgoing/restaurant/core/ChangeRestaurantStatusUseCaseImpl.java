package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantStatus;
import be.kdg.keepdishesgoing.restaurant.port.in.ChangeRestaurantStatusUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ChangeRestaurantStatusCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import be.kdg.keepdishesgoing.restaurant.port.out.UpdateRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChangeRestaurantStatusUseCaseImpl implements ChangeRestaurantStatusUseCase {

    private final LoadRestaurantPort loadRestaurantPort;
    private final UpdateRestaurantPort updateRestaurantPort;
    private final PublishEventPort publishEventPort;

    public ChangeRestaurantStatusUseCaseImpl(LoadRestaurantPort loadRestaurantPort, UpdateRestaurantPort updateRestaurantPort, PublishEventPort publishEventPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.updateRestaurantPort = updateRestaurantPort;
        this.publishEventPort = publishEventPort;
    }

    @Override
    public void changeRestaurantStatus(ChangeRestaurantStatusCommand command) {
        var restaurantId = RestaurantId.of(command.restaurantId());
        Restaurant restaurant = loadRestaurantPort.loadById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        if (command.newStatus() == RestaurantStatus.MANUALLY_OPENED) {
            restaurant.manuallyOpen();
        } else if (command.newStatus() == RestaurantStatus.MANUALLY_CLOSED) {
            restaurant.manuallyClose();
        } else {
            throw new IllegalArgumentException("Invalid manual state change: " + command.newStatus());
        }

        updateRestaurantPort.updateRestaurant(restaurant);

        publishEventPort.publishAll(restaurant.getUncommittedEvents());

        // Clear events
        restaurant.markEventAsCommitted();
    }

}
