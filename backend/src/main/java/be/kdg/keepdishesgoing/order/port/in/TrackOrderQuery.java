package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.queries.OrderTracking;

import java.util.UUID;

/**
 * Follows one order from placement to delivery (US-24, US-25).
 */
public interface TrackOrderQuery {

    /**
     * @throws be.kdg.keepdishesgoing.common.exceptions.NotFoundException
     *         if no order with that id was ever placed
     */
    OrderTracking track(UUID orderId);
}
