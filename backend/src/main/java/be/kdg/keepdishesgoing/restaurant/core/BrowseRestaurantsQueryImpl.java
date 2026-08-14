package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;
import be.kdg.keepdishesgoing.restaurant.port.in.BrowseRestaurantsQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.MenuItem;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.RestaurantSearchQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.RestaurantSummary;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Builds the customer's restaurant list.
 *
 * <p>Filtering happens here rather than in a query because two of the three
 * criteria are domain rules, not columns: whether a restaurant is open right now
 * follows from its schedule plus any manual override, and its price range
 * follows from the dishes currently on its menu. Expressing either in JPQL would
 * mean restating {@code Restaurant.isOpen} in SQL, where it would quietly drift
 * from the version the rest of the application obeys.
 *
 * <p>The cost is two queries for the whole catalogue. That is acceptable for a
 * marketplace of this size and, importantly, it is two queries and not one per
 * restaurant.
 */
@Service
@Transactional(readOnly = true)
public class BrowseRestaurantsQueryImpl implements BrowseRestaurantsQuery {

    private final LoadRestaurantPort loadRestaurantPort;
    private final LoadDishPort loadDishPort;

    public BrowseRestaurantsQueryImpl(LoadRestaurantPort loadRestaurantPort, LoadDishPort loadDishPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.loadDishPort = loadDishPort;
    }

    @Override
    public List<RestaurantSummary> browse(RestaurantSearchQuery query) {
        LocalDateTime now = LocalDateTime.now();
        Map<UUID, List<MenuItem>> menusByRestaurant = loadMenus();

        return loadRestaurantPort.loadAll().stream()
                .filter(restaurant -> matchesCuisine(restaurant.getCuisineType(), query))
                .filter(restaurant -> !query.openNow() || restaurant.isOpen(now))
                .map(restaurant -> RestaurantSummary.from(
                        restaurant,
                        menusByRestaurant.getOrDefault(restaurant.getRestaurantId().uuid(), List.of()),
                        now))
                .filter(summary -> matchesPrice(summary, query, menusByRestaurant))
                .sorted(Comparator.comparing(RestaurantSummary::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /** Every customer-visible dish in the catalogue, grouped by restaurant. */
    private Map<UUID, List<MenuItem>> loadMenus() {
        return loadDishPort.loadAll().stream()
                .filter(Dish::isOnMenu)
                .map(MenuItem::from)
                .collect(Collectors.groupingBy(MenuItem::restaurantId));
    }

    private boolean matchesCuisine(CuisineType cuisineType, RestaurantSearchQuery query) {
        return query.cuisineType() == null || query.cuisineType() == cuisineType;
    }

    /**
     * A price filter asks "can I eat here for that?", so one dish inside the
     * range is enough: requiring the whole menu to fit would exclude any
     * restaurant that also sells a lobster.
     */
    private boolean matchesPrice(RestaurantSummary summary,
                                 RestaurantSearchQuery query,
                                 Map<UUID, List<MenuItem>> menusByRestaurant) {
        if (!query.hasPriceFilter()) {
            return true;
        }
        return menusByRestaurant.getOrDefault(summary.restaurantId(), List.of()).stream()
                .map(MenuItem::price)
                .anyMatch(price -> withinRange(price, query.minPrice(), query.maxPrice()));
    }

    private boolean withinRange(BigDecimal price, BigDecimal min, BigDecimal max) {
        return (min == null || price.compareTo(min) >= 0)
                && (max == null || price.compareTo(max) <= 0);
    }
}
