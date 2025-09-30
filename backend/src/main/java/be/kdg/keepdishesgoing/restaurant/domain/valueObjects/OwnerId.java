package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.util.UUID;

public record OwnerId(UUID uuid) {

    public static OwnerId of(UUID uuid) {
        return new OwnerId(uuid);
    }

    public static OwnerId create() {
        return new OwnerId(UUID.randomUUID());
    }
}
