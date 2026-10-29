package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.port.in.ListRestaurantOrdersQuery;
import be.kdg.keepdishesgoing.order.port.in.queries.RestaurantOrderSummary;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderSummaryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListRestaurantOrdersQueryImpl implements ListRestaurantOrdersQuery {

    private final LoadOrderSummaryPort loadOrderSummaryPort;

    public ListRestaurantOrdersQueryImpl(LoadOrderSummaryPort loadOrderSummaryPort) {
        this.loadOrderSummaryPort = loadOrderSummaryPort;
    }

    @Override
    public List<RestaurantOrderSummary> listOrders(UUID restaurantId) {
        return loadOrderSummaryPort.loadByRestaurant(restaurantId)
                .stream()
                .map(RestaurantOrderSummary::from)
                .toList();
    }
}
