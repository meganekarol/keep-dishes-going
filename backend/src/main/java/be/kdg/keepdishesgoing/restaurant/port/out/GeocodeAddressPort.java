package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Coordinates;

import java.util.Optional;

/**
 * Turns a written address into a point on the map (US-19).
 */
public interface GeocodeAddressPort {

    /**
     * @return empty when the address could not be placed, which includes the
     *         service being unreachable. Never throws
     */
    Optional<Coordinates> locate(Address address);
}
