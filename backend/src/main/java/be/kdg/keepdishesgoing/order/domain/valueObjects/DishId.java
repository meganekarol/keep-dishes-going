package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.util.UUID;

/**
 * Identifies one dish, as the order context knows it.
 *
 * @param uuid the identifier itself
 */
public record DishId(UUID uuid) {

    /** @throws IllegalArgumentException if the identifier is missing */
    public DishId {
        if (uuid == null) {
            throw new IllegalArgumentException("Dish ID cannot be null");
        }
    }

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
     * Reads an identifier from text.
     *
     * @param uuid the identifier written out
     * @return a DishId holding that identifier
     */
    public static DishId of(String uuid) {
        return new DishId(UUID.fromString(uuid));
    }
}
