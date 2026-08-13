package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;
import be.kdg.keepdishesgoing.restaurant.port.in.BrowseRestaurantsQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.ViewRestaurantMenuQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * The public catalogue: browsing restaurants and reading their menus
 * (US-14 … US-18).
 *
 * <p>Deliberately separate from {@link RestaurantController}, which is the
 * owner's side of the same aggregate. The two have different audiences,
 * different security, and different shapes — an owner sees drafts and a stored
 * status, a customer sees a published menu and whether the door is open.
 *
 * <p>These endpoints return the read models from {@code port.in.queries}
 * unchanged. There is no separate response DTO because the read model already
 * <em>is</em> one: unlike the owner endpoints, nothing here hands out an
 * aggregate that would need flattening first.
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantCatalogController {

    private final BrowseRestaurantsQuery browseRestaurantsQuery;
    private final ViewRestaurantMenuQuery viewRestaurantMenuQuery;

    public RestaurantCatalogController(BrowseRestaurantsQuery browseRestaurantsQuery,
                                       ViewRestaurantMenuQuery viewRestaurantMenuQuery) {
        this.browseRestaurantsQuery = browseRestaurantsQuery;
        this.viewRestaurantMenuQuery = viewRestaurantMenuQuery;
    }

    /**
     * Browse the catalogue, optionally narrowed by cuisine, opening state or
     * price (US-14, US-16).
     */
    @GetMapping
    public ResponseEntity<List<RestaurantSummary>> browseRestaurants(
            @RequestParam(required = false) CuisineType cuisineType,
            @RequestParam(defaultValue = "false") boolean openNow,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        RestaurantSearchQuery query = new RestaurantSearchQuery(cuisineType, openNow, minPrice, maxPrice);
        return ResponseEntity.ok(browseRestaurantsQuery.browse(query));
    }

    /**
     * One restaurant with its full menu (US-15).
     */
    @GetMapping("/{restaurantId}")
    public ResponseEntity<RestaurantDetail> viewRestaurant(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(viewRestaurantMenuQuery.viewRestaurant(restaurantId));
    }

    /**
     * The same menu, filtered by dish type and food tags and sorted
     * (US-17, US-18).
     *
     * <p>{@code foodTags} takes a comma-separated list, e.g.
     * {@code ?foodTags=VEGAN,GLUTEN_FREE}.
     */
    @GetMapping("/{restaurantId}/dishes")
    public ResponseEntity<List<MenuItem>> browseMenu(
            @PathVariable UUID restaurantId,
            @RequestParam(required = false) DishType type,
            @RequestParam(required = false) Set<FoodTag> foodTags,
            @RequestParam(required = false) DishSortOrder sort) {

        DishSearchQuery query = new DishSearchQuery(restaurantId, type, foodTags, sort);
        return ResponseEntity.ok(viewRestaurantMenuQuery.browseMenu(query));
    }
}
