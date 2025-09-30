package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.util.UUID;

public record RestaurantId(UUID uuid) {

    public static RestaurantId of(UUID uuid) {
        return new RestaurantId(uuid);
    }

    public static RestaurantId create() {
        return new RestaurantId(UUID.randomUUID());
    }
}
