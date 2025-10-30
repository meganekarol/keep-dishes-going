package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.util.UUID;

public record RestaurantId(UUID uuid) {
    public RestaurantId {
        if (uuid == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
    }

    public static RestaurantId of(UUID uuid) {
        return new RestaurantId(uuid);
    }

    public static RestaurantId of(String uuid) {
        return new RestaurantId(UUID.fromString(uuid));
    }
}
