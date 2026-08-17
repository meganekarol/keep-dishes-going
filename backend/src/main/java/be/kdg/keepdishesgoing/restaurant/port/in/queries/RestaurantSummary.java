package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import be.kdg.keepdishesgoing.common.DeliveryEstimate;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Coordinates;
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
 * @param guesstimatedDeliveryTime
 *                      minutes of cooking plus the ride, multiplied by how
 *                      busy the kitchen is, so a customer can compare
 *                      restaurants before ordering (US-19)
 */
public record RestaurantSummary(
        UUID restaurantId,
        String name,
        CuisineType cuisineType,
        Address address,
        List<String> pictureUrls,
        int defaultPreparationTime,
        int guesstimatedDeliveryTime,
        boolean open,
        BigDecimal lowestPrice,
        BigDecimal highestPrice,
        int menuSize
) {

    /**
     * @param customerLocation where the browser says the customer is, or
     *                         {@code null} when they did not share it
     * @param ordersNotReady   how many orders this kitchen still has to cook
     */
    public static RestaurantSummary from(Restaurant restaurant, List<MenuItem> menu, LocalDateTime now,
                                         Coordinates customerLocation, int ordersNotReady) {
        return new RestaurantSummary(
                restaurant.getRestaurantId().uuid(),
                restaurant.getName(),
                restaurant.getCuisineType(),
                restaurant.getAddress(),
                restaurant.getPictureUrls(),
                restaurant.getDefaultPrepTime(),
                DeliveryEstimate.totalMinutes(
                        distanceBetween(restaurant.getAddress(), customerLocation),
                        restaurant.getDefaultPrepTime(),
                        ordersNotReady),
                restaurant.isOpen(now),
                menu.stream().map(MenuItem::price).min(Comparator.naturalOrder()).orElse(null),
                menu.stream().map(MenuItem::price).max(Comparator.naturalOrder()).orElse(null),
                menu.size()
        );
    }

    /**
     * Kilometres between the two, or {@code null} when either end is unplaced:
     * the restaurant was never geocoded, or the customer said no to sharing
     * their location.
     */
    static Double distanceBetween(Address address, Coordinates customerLocation) {
        if (address == null || address.coordinates() == null || customerLocation == null) {
            return null;
        }
        return address.coordinates().distanceInKmTo(customerLocation);
    }
}
