package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.util.UUID;

public record DishId(UUID uuid) {

    public static DishId of(UUID uuid) {
        return new DishId(uuid);
    }

    public static DishId create() {
        return new DishId(UUID.randomUUID());
    }
}
