package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.port.in.queries.OwnerMenu;

import java.util.UUID;

/**
 * The menu as its owner manages it (US-03).
 *
 * <p>Separate from {@link ViewRestaurantMenuUseCase} because the two answer
 * different questions. A customer asks what they can order; an owner asks what
 * they have, what is live, and what is still waiting to go live.
 */
public interface ViewOwnerMenuUseCase {

    /**
     * @throws be.kdg.keepdishesgoing.common.exceptions.NotFoundException
     *         if no such restaurant exists
     */
    OwnerMenu viewOwnerMenu(UUID restaurantId);
}
