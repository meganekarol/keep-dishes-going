package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.util.UUID;

/**
 * Identifies one restaurant.
 *
 * @param uuid the identifier itself
 */
public record RestaurantId(UUID uuid) {

    /**
     * Wraps an existing identifier.
     *
     * @param uuid the identifier to wrap
     * @return a RestaurantId holding that identifier
     */
    public static RestaurantId of(UUID uuid) {
        return new RestaurantId(uuid);
    }

    /**
     * Makes a brand new identifier.
     *
     * @return a RestaurantId holding a random identifier
     */
    public static RestaurantId create() {
        return new RestaurantId(UUID.randomUUID());
    }
}
