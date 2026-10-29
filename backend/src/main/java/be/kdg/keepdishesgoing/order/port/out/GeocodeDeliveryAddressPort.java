package be.kdg.keepdishesgoing.order.port.out;

import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.domain.valueObjects.GeoLocation;

import java.util.Optional;

/**
 * Places the customer's delivery address on the map.
 *
 * <p>Needed because an accepted order is published with a dropoff point for
 * the courier (US-28).
 */
public interface GeocodeDeliveryAddressPort {

    /**
     * @return empty when the address could not be placed, which includes the
     *         service being unreachable. Never throws: a customer must still be
     *         able to order when a map server is down.
     */
    Optional<GeoLocation> locate(DeliveryAddress address);
}
