package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import java.util.List;
import java.util.UUID;

/**
 * An owner's whole menu, with the two numbers they need to manage it (US-03).
 *
 * @param pendingChangesCount how many dishes have edits that are not on the live
 *                            menu yet. Kept alongside the dishes rather than
 *                            counted in the browser so the figure cannot drift
 *                            from the rule that produced it.
 * @param publishedCount      how many dishes are live, counted from the menu
 *                            itself rather than from dish states, because that
 *                            is the list the cap is enforced against.
 * @param maxPublishedDishes  the cap, so the screen can say "7 of 10" without
 *                            repeating the number.
 */
public record OwnerMenu(
        UUID restaurantId,
        List<OwnerDishView> dishes,
        int pendingChangesCount,
        int publishedCount,
        int maxPublishedDishes
) {
}
