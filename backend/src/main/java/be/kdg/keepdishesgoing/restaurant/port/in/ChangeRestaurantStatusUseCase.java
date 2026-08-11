package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.ChangeRestaurantStatusCommand;

import java.util.UUID;

public interface ChangeRestaurantStatusUseCase {
    void changeRestaurantStatus(ChangeRestaurantStatusCommand command);

    /** Drop a manual override so the weekly schedule decides again (US-09). */
    void followSchedule(UUID restaurantId);
}
