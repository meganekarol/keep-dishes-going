package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.CheckRestaurantOwnershipUseCase;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Bean name is referenced from {@code @PreAuthorize} expressions, so it is
 * pinned here rather than left to Spring's default naming.
 */
@Service("restaurantOwnership")
@Transactional(readOnly = true)
public class CheckRestaurantOwnershipUseCaseImpl implements CheckRestaurantOwnershipUseCase {

    private final LoadRestaurantPort loadRestaurantPort;

    public CheckRestaurantOwnershipUseCaseImpl(LoadRestaurantPort loadRestaurantPort) {
        this.loadRestaurantPort = loadRestaurantPort;
    }

    @Override
    public boolean isOwner(UUID restaurantId, UUID ownerId) {
        if (restaurantId == null || ownerId == null) {
            return false;
        }
        return loadRestaurantPort.loadById(RestaurantId.of(restaurantId))
                .map(Restaurant::getOwnerId)
                .filter(owner -> owner.uuid().equals(ownerId))
                .isPresent();
    }

    @Override
    public boolean isOwner(UUID restaurantId, String ownerId) {
        try {
            return isOwner(restaurantId, UUID.fromString(ownerId));
        } catch (IllegalArgumentException | NullPointerException e) {
            // A principal whose name is not a UUID cannot own anything. Denying
            // is the only safe reading, and it must not surface as a 500.
            return false;
        }
    }
}
