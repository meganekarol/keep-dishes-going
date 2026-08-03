package be.kdg.keepdishesgoing.restaurant.port.in;

import java.util.UUID;

/**
 * Answers "does this owner own this restaurant?".
 *
 * <p>Owner routes carry the restaurant id in the path, so the Keycloak role
 * alone only proves the caller is <em>an</em> owner, not that they own
 * <em>this</em> restaurant. That second question needs a lookup, which a URL
 * matcher cannot do — hence a use case the web layer can call from
 * {@code @PreAuthorize}.
 */
public interface CheckRestaurantOwnershipUseCase {

    boolean isOwner(UUID restaurantId, UUID ownerId);

    /**
     * Convenience overload for SpEL, where the authenticated principal's name
     * is the JWT {@code sub} claim as a String.
     */
    boolean isOwner(UUID restaurantId, String ownerId);
}
