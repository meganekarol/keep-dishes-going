package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishState;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishVersion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One dish as its owner sees it: both versions, side by side.
 *
 * <p> Owner have to be able to tell what
 * is live from what is still waiting, so both versions travel together and
 * either one may be absent. A dish that has never gone live has no
 * {@code published}; a dish with no unsaved edits has no {@code draft}.
 *
 * @param pendingChanges whether the draft differs from what customers can order
 *                       right now. This is the flag US-03's running count is
 *                       built from.
 */
public record OwnerDishView(
        UUID dishId,
        DishState status,
        DishVersion published,
        DishVersion draft,
        boolean pendingChanges,
        LocalDateTime scheduledPublishAt,
        LocalDateTime lastPublishedAt
) {

    public static OwnerDishView from(Dish dish) {
        return new OwnerDishView(
                dish.getDishId().uuid(),
                dish.getStatus(),
                dish.getPublishedVersion(),
                dish.getDraftVersion(),
                dish.hasPendingChanges(),
                dish.getScheduledPublishAt(),
                dish.getLastPublishedAt()
        );
    }
}
