package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object representing a unique Order identifier
 */
public record OrderId(UUID uuid) {

    public OrderId {
        Objects.requireNonNull(uuid, "Order ID cannot be null");
    }

    public static OrderId create() {
        return new OrderId(UUID.randomUUID());
    }

    public static OrderId of(String uuid) {
        return new OrderId(UUID.fromString(uuid));
    }

    public static OrderId of(UUID uuid) {
        return new OrderId(uuid);
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}