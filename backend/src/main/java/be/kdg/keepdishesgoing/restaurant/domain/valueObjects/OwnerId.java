package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

import java.util.UUID;

/**
 * Identifies the owner a restaurant belongs to.
 *
 * @param uuid the identifier itself
 */
public record OwnerId(UUID uuid) {

    /** A fixed owner used while developing, so there is always one to sign in as. */
    public static final OwnerId BOBBY = OwnerId.of(UUID.fromString("8c8c87c1-9b5f-4f73-9fa7-7f9b5c7f2296"));

    /**
     * Wraps an existing identifier.
     *
     * @param uuid the identifier to wrap
     * @return a OwnerId holding that identifier
     */
    public static OwnerId of(UUID uuid) {
        return new OwnerId(uuid);
    }

    /**
     * Makes a brand new identifier.
     *
     * @return a OwnerId holding a random identifier
     */
    public static OwnerId create() {
        return new OwnerId(UUID.randomUUID());
    }
}
