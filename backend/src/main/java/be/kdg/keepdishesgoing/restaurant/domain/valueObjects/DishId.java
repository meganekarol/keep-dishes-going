package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.util.UUID;

/**
 * Identifies one dish.
 *
 * @param uuid the identifier itself
 */
public record DishId(UUID uuid) {

    /**
     * Wraps an existing identifier.
     *
     * @param uuid the identifier to wrap
     * @return a DishId holding that identifier
     */
    public static DishId of(UUID uuid) {
        return new DishId(uuid);
    }

    /**
     * Makes a brand new identifier.
     *
     * @return a DishId holding a random identifier
     */
    public static DishId create() {
        return new DishId(UUID.randomUUID());
    }
}
