package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantStatus;
import be.kdg.keepdishesgoing.restaurant.port.in.ChangeRestaurantStatusUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ChangeRestaurantStatusCommand;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import be.kdg.keepdishesgoing.restaurant.port.out.SaveRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class ChangeRestaurantStatusUseCaseImpl implements ChangeRestaurantStatusUseCase {

    private final LoadRestaurantPort loadRestaurantPort;
    private final SaveRestaurantPort saveRestaurantPort;
    private final PublishEventPort publishEventPort;

    public ChangeRestaurantStatusUseCaseImpl(LoadRestaurantPort loadRestaurantPort, SaveRestaurantPort saveRestaurantPort, PublishEventPort publishEventPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.saveRestaurantPort = saveRestaurantPort;
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

        saveRestaurantPort.saveRestaurant(restaurant);

        publishEventPort.publishAll(restaurant.getUncommittedEvents());

        // Clear events
        restaurant.markEventAsCommitted();
    }

    @Override
    public void followSchedule(UUID restaurantId) {
        Restaurant restaurant = loadRestaurantPort.loadById(RestaurantId.of(restaurantId))
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));

        restaurant.followSchedule(LocalDateTime.now());

        saveRestaurantPort.saveRestaurant(restaurant);
        publishEventPort.publishAll(restaurant.getUncommittedEvents());
        restaurant.markEventAsCommitted();
    }

}
