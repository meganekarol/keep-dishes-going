package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.ChangeRestaurantStatusCommand;

public interface ChangeRestaurantStatusUseCase {
    void changeRestaurantStatus(ChangeRestaurantStatusCommand command);
}
