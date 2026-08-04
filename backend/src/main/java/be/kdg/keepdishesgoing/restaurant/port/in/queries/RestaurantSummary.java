package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * A restaurant as it appears in the customer's browse list (US-14).
 *
 * @param open          evaluated against the clock, never read from the stored
 *                      status: that column only moves when an owner toggles it,
 *                      so a restaurant open on its normal schedule would
 *                      otherwise look closed.
 * @param lowestPrice   cheapest dish on the menu, {@code null} for an empty menu
 * @param highestPrice  dearest dish on the menu, {@code null} for an empty menu
 * @param menuSize      how many dishes a customer can see, sold-out ones included
 */
public record RestaurantSummary(
        UUID restaurantId,
        String name,
        CuisineType cuisineType,
        Address address,
        List<String> pictureUrls,
        int defaultPreparationTime,
        boolean open,
        BigDecimal lowestPrice,
        BigDecimal highestPrice,
        int menuSize
) {

    public static RestaurantSummary from(Restaurant restaurant, List<MenuItem> menu, LocalDateTime now) {
        return new RestaurantSummary(
                restaurant.getRestaurantId().uuid(),
                restaurant.getName(),
                restaurant.getCuisineType(),
                restaurant.getAddress(),
                restaurant.getPictureUrls(),
                restaurant.getDefaultPrepTime(),
                restaurant.isOpen(now),
                menu.stream().map(MenuItem::price).min(Comparator.naturalOrder()).orElse(null),
                menu.stream().map(MenuItem::price).max(Comparator.naturalOrder()).orElse(null),
                menu.size()
        );
    }
}
