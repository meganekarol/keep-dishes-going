package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.UnpublishDishCommand;

/** Lets an owner put a dish on the menu or take it off (US-04, US-05). */
public interface PublishAndUnpublishDishUseCase {
    /**
     * Puts a dish on the menu, so customers can order it.
     *
     * @param command which dish to publish
     */
    void publishDish(PublishDishCommand command);
    /**
     * Takes a dish off the menu.
     *
     * @param command which dish to unpublish
     */
    void unpublishDish(UnpublishDishCommand command);
}
