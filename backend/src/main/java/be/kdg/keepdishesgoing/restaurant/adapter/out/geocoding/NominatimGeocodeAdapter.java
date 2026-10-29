package be.kdg.keepdishesgoing.restaurant.adapter.out.geocoding;

import be.kdg.keepdishesgoing.common.geocoding.GeocodingClient;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Coordinates;
import be.kdg.keepdishesgoing.restaurant.port.out.GeocodeAddressPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Places a restaurant's address, using the shared geocoding client.
 *
 * <p>The mapping to this context's own {@link Coordinates} happens here, so
 * the restaurant core never sees the shared kernel's point type either.
 */
@Component
public class NominatimGeocodeAdapter implements GeocodeAddressPort {

    private final GeocodingClient geocodingClient;

    public NominatimGeocodeAdapter(GeocodingClient geocodingClient) {
        this.geocodingClient = geocodingClient;
    }

    @Override
    public Optional<Coordinates> locate(Address address) {
        return geocodingClient.locate(address.asSearchText())
                .map(point -> new Coordinates(point.lat(), point.lng()));
    }
}
