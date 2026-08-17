package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.common.exceptions.NotFoundException;
import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.TrackOrderQuery;
import be.kdg.keepdishesgoing.order.port.in.queries.OrderTracking;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderSummaryPort;
import be.kdg.keepdishesgoing.order.port.out.LoadRestaurantProjectionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TrackOrderQueryImpl implements TrackOrderQuery {

    private final LoadOrderPort loadOrderPort;
    private final LoadRestaurantProjectionPort loadRestaurantProjectionPort;
    private final LoadOrderSummaryPort loadOrderSummaryPort;

    public TrackOrderQueryImpl(LoadOrderPort loadOrderPort,
                                 LoadRestaurantProjectionPort loadRestaurantProjectionPort,
                                 LoadOrderSummaryPort loadOrderSummaryPort) {
        this.loadOrderPort = loadOrderPort;
        this.loadRestaurantProjectionPort = loadRestaurantProjectionPort;
        this.loadOrderSummaryPort = loadOrderSummaryPort;
    }

    @Override
    public OrderTracking track(UUID orderId) {
        Order order = loadOrderPort.loadBy(OrderId.of(orderId))
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        UUID restaurantId = order.getRestaurantId().uuid();
        String restaurantName = loadRestaurantProjectionPort.getRestaurantName(restaurantId);
        int preparationMinutes = loadRestaurantProjectionPort.getPreparationTime(restaurantId);
        int ordersNotReady = loadOrderSummaryPort.countNotReadyForRestaurant(restaurantId);

        return OrderTracking.from(order, restaurantName, preparationMinutes, ordersNotReady);
    }
}
