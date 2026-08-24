package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifies one order. This is also what a customer tracks their order with.
 *
 * @param uuid the identifier itself
 */
public record OrderId(UUID uuid) {

    /** @throws NullPointerException if the identifier is missing */
    public OrderId {
        Objects.requireNonNull(uuid, "Order ID cannot be null");
    }

    /**
     * Makes a brand new identifier.
     *
     * @return an OrderId holding a random identifier
     */
    public static OrderId create() {
        return new OrderId(UUID.randomUUID());
    }

    /**
     * Reads an identifier from text.
     *
     * @param uuid the identifier written out
     * @return an OrderId holding that identifier
     */
    public static OrderId of(String uuid) {
        return new OrderId(UUID.fromString(uuid));
    }

    /**
     * Wraps an existing identifier.
     *
     * @param uuid the identifier to wrap
     * @return an OrderId holding that identifier
     */
    public static OrderId of(UUID uuid) {
        return new OrderId(uuid);
    }

    /**
     * Writes the identifier out as text.
     *
     * @return the identifier in its usual written form
     */
    @Override
    public String toString() {
        return uuid.toString();
    }
}
