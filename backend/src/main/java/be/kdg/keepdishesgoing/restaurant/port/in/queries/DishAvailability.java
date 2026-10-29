package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;

import java.util.UUID;

/**
 * Whether one dish in a basket can still be ordered (US-21).
 */
public record DishAvailability(UUID dishId, Availability availability) {

    public enum Availability {
        AVAILABLE,
        /** Still on the menu, but the kitchen has run out. */
        OUT_OF_STOCK,
        /** Unpublished, or never on this restaurant's menu at all. */
        REMOVED
    }

    public static DishAvailability of(Dish dish) {
        Availability availability = switch (dish.getStatus()) {
            case PUBLISHED -> Availability.AVAILABLE;
            case OUT_OF_STOCK -> Availability.OUT_OF_STOCK;
            case DRAFT, UNPUBLISHED -> Availability.REMOVED;
        };
        return new DishAvailability(dish.getDishId().uuid(), availability);
    }

    /**
     * A dish id this restaurant cannot serve: unknown, or belonging to another
     * restaurant. The customer is told the same thing either way, because from
     * the basket's side there is no difference.
     */
    public static DishAvailability removed(UUID dishId) {
        return new DishAvailability(dishId, Availability.REMOVED);
    }
}
