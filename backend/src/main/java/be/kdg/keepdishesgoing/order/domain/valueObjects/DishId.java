package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.util.UUID;

public record DishId(UUID uuid) {
    public DishId {
        if (uuid == null) {
            throw new IllegalArgumentException("Dish ID cannot be null");
        }
    }

    public static DishId of(UUID uuid) {
        return new DishId(uuid);
    }

    public static DishId of(String uuid) {
        return new DishId(UUID.fromString(uuid));
    }
}
