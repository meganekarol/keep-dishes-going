package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.util.UUID;

/**
 * Identifies one restaurant, as the order context knows it.
 *
 * @param uuid the identifier itself
 */
public record RestaurantId(UUID uuid) {

    /** @throws IllegalArgumentException if the identifier is missing */
    public RestaurantId {
        if (uuid == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
    }

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
     * Reads an identifier from text.
     *
     * @param uuid the identifier written out
     * @return a RestaurantId holding that identifier
     */
    public static RestaurantId of(String uuid) {
        return new RestaurantId(UUID.fromString(uuid));
    }
}
