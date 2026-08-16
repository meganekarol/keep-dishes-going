package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.queries.RestaurantOrderSummary;

import java.util.List;
import java.util.UUID;

/**
 * Lists the orders placed at one restaurant (US-10).
 */
public interface ListRestaurantOrdersQuery {

    List<RestaurantOrderSummary> listOrders(UUID restaurantId);
}
