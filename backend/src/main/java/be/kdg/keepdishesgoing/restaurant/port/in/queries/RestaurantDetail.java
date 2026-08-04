package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * One restaurant's page: everything US-15 asks a customer to see before
 * ordering — where it is, when it opens, and what is on the menu.
 *
 * <p>The weekly schedule is included on purpose: a customer looking at a closed
 * restaurant needs to know when to come back, and the {@code open} flag alone
 * cannot tell them that.
 */
public record RestaurantDetail(
        UUID restaurantId,
        String name,
        CuisineType cuisineType,
        Address address,
        String contactEmail,
        List<String> pictureUrls,
        int defaultPreparationTime,
        boolean open,
        Map<DayOfWeek, TimeRange> openingHours,
        List<MenuItem> menu
) {

    public static RestaurantDetail from(Restaurant restaurant, List<MenuItem> menu, LocalDateTime now) {
        return new RestaurantDetail(
                restaurant.getRestaurantId().uuid(),
                restaurant.getName(),
                restaurant.getCuisineType(),
                restaurant.getAddress(),
                restaurant.getContactEmail(),
                restaurant.getPictureUrls(),
                restaurant.getDefaultPrepTime(),
                restaurant.isOpen(now),
                restaurant.getOpeningHours().schedule(),
                menu
        );
    }
}
