package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;

import java.math.BigDecimal;

/**
 * The filters a customer can put on the restaurant list (US-16).
 *
 * <p>Every field is optional; a query with nothing set returns the whole
 * catalogue. Distance and estimated delivery time are the two filters US-16 also
 * names — they need geocoordinates on {@code Address}, which do not exist yet,
 * so they are not offered rather than silently ignored.
 *
 * @param cuisineType only this cuisine, {@code null} for all
 * @param openNow     {@code true} keeps only restaurants taking orders right now
 * @param minPrice    keep restaurants offering at least one dish at or above this
 * @param maxPrice    keep restaurants offering at least one dish at or below this
 */
public record RestaurantSearchQuery(
        CuisineType cuisineType,
        boolean openNow,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {

    public static RestaurantSearchQuery unfiltered() {
        return new RestaurantSearchQuery(null, false, null, null);
    }

    public boolean hasPriceFilter() {
        return minPrice != null || maxPrice != null;
    }
}
