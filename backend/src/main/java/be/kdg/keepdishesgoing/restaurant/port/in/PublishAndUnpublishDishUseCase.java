package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.UnpublishDishCommand;

public interface PublishAndUnpublishDishUseCase {
    void publishDish(PublishDishCommand command);
    void unpublishDish(UnpublishDishCommand command);
}
