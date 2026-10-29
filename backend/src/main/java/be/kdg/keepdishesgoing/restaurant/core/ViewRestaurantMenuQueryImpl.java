package be.kdg.keepdishesgoing.restaurant.core;

import be.kdg.keepdishesgoing.common.exceptions.NotFoundException;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Coordinates;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.restaurant.port.in.ViewRestaurantMenuQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.DishAvailability;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.DishSearchQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.MenuItem;
import be.kdg.keepdishesgoing.restaurant.port.in.queries.RestaurantDetail;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadDishPort;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantBusynessPort;
import be.kdg.keepdishesgoing.restaurant.port.out.LoadRestaurantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Serves one restaurant's page and its menu.
 *
 * <p>Both entry points check the restaurant exists first, so a customer
 * following a stale link gets a 404 instead of an empty menu that looks like a
 * restaurant with nothing to sell.
 */
@Service
@Transactional(readOnly = true)
public class ViewRestaurantMenuQueryImpl implements ViewRestaurantMenuQuery {

    private final LoadRestaurantPort loadRestaurantPort;
    private final LoadDishPort loadDishPort;
    private final LoadRestaurantBusynessPort loadRestaurantBusynessPort;

    public ViewRestaurantMenuQueryImpl(LoadRestaurantPort loadRestaurantPort, LoadDishPort loadDishPort,
                                       LoadRestaurantBusynessPort loadRestaurantBusynessPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.loadDishPort = loadDishPort;
        this.loadRestaurantBusynessPort = loadRestaurantBusynessPort;
    }

    @Override
    public RestaurantDetail viewRestaurant(UUID restaurantId, Coordinates customerLocation) {
        Restaurant restaurant = load(restaurantId);
        List<MenuItem> menu = menuOf(restaurantId, DishSearchQuery.wholeMenu(restaurantId));
        return RestaurantDetail.from(
                restaurant,
                menu,
                LocalDateTime.now(),
                customerLocation,
                loadRestaurantBusynessPort.ordersNotReadyFor(restaurantId));
    }

    @Override
    public List<MenuItem> browseMenu(DishSearchQuery query) {
        load(query.restaurantId());
        return menuOf(query.restaurantId(), query);
    }

    @Override
    public List<DishAvailability> checkAvailability(UUID restaurantId, List<UUID> dishIds) {
        load(restaurantId);

        Map<UUID, Dish> menu = loadDishPort.loadByRestaurant(RestaurantId.of(restaurantId)).stream()
                .collect(Collectors.toMap(dish -> dish.getDishId().uuid(), Function.identity()));

        // Every id gets an answer, even one this restaurant has never heard of:
        // a basket asking about a dish that is gone still needs telling.
        return dishIds.stream()
                .distinct()
                .map(dishId -> menu.containsKey(dishId)
                        ? DishAvailability.of(menu.get(dishId))
                        : DishAvailability.removed(dishId))
                .toList();
    }

    private Restaurant load(UUID restaurantId) {
        return loadRestaurantPort.loadById(RestaurantId.of(restaurantId))
                .orElseThrow(() -> new NotFoundException("Restaurant", restaurantId));
    }

    private List<MenuItem> menuOf(UUID restaurantId, DishSearchQuery query) {
        return loadDishPort.loadByRestaurant(RestaurantId.of(restaurantId)).stream()
                .filter(Dish::isOnMenu)
                .map(MenuItem::from)
                .filter(item -> query.type() == null || item.type() == query.type())
                .filter(item -> item.foodTags().containsAll(query.foodTags()))
                .sorted(query.sortOrder().comparator())
                .toList();
    }
}
