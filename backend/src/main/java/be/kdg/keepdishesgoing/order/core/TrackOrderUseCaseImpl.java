package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.common.exceptions.NotFoundException;
import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.TrackOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.queries.OrderTracking;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.LoadRestaurantProjectionPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Answers the tracking query straight from the event store.
 *
 * <p>No separate read model: the question is "what is the state of <em>this</em>
 * aggregate?", which is exactly what rehydration answers, and the snapshot
 * mechanism already keeps that cheap. A projection would buy nothing here and
 * would add a second copy of the truth that could fall behind the events —
 * it only starts to earn its keep for queries that span aggregates, such as
 * listing every pending order for a restaurant.
 *
 * <p>The restaurant's name comes from the Order context's own projection rather
 * than the Restaurant context, so this query never reaches across the context
 * boundary at read time.
 */
@Service
@Transactional(readOnly = true)
public class TrackOrderUseCaseImpl implements TrackOrderUseCase {

    private final LoadOrderPort loadOrderPort;
    private final LoadRestaurantProjectionPort loadRestaurantProjectionPort;

    public TrackOrderUseCaseImpl(LoadOrderPort loadOrderPort,
                                 LoadRestaurantProjectionPort loadRestaurantProjectionPort) {
        this.loadOrderPort = loadOrderPort;
        this.loadRestaurantProjectionPort = loadRestaurantProjectionPort;
    }

    @Override
    public OrderTracking track(UUID orderId) {
        Order order = loadOrderPort.loadBy(OrderId.of(orderId))
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        String restaurantName = loadRestaurantProjectionPort
                .getRestaurantName(order.getRestaurantId().uuid());

        return OrderTracking.from(order, restaurantName);
    }
}
