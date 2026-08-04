package be.kdg.keepdishesgoing.restaurant.port.in.queries;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * The filters and ordering a customer can put on one restaurant's menu
 * (US-17, US-18).
 *
 * @param type      only starters, mains or desserts; {@code null} for all
 * @param foodTags  a dish must carry <em>all</em> of these to match. Dietary
 *                  filters combine that way: someone asking for vegan and
 *                  gluten-free can eat neither a vegan-only nor a
 *                  gluten-free-only dish. An empty set filters nothing.
 * @param sortOrder defaults to {@link DishSortOrder#NAME} so the menu has a
 *                  stable order even when the customer has not chosen one
 */
public record DishSearchQuery(
        UUID restaurantId,
        DishType type,
        Set<FoodTag> foodTags,
        DishSortOrder sortOrder
) {

    public DishSearchQuery {
        Objects.requireNonNull(restaurantId, "Restaurant id is required");
        foodTags = foodTags == null ? Set.of() : Set.copyOf(foodTags);
        sortOrder = sortOrder == null ? DishSortOrder.NAME : sortOrder;
    }

    public static DishSearchQuery wholeMenu(UUID restaurantId) {
        return new DishSearchQuery(restaurantId, null, Set.of(), DishSortOrder.NAME);
    }
}
