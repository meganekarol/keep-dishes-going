package be.kdg.keepdishesgoing.order.adapter.out.geocoding;

import be.kdg.keepdishesgoing.common.geocoding.GeocodingClient;
import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.domain.valueObjects.GeoLocation;
import be.kdg.keepdishesgoing.order.port.out.GeocodeDeliveryAddressPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Places a delivery address, using the shared geocoding client.
 *
 * <p>The mapping to this context's own {@link GeoLocation} happens here, so the
 * order core never sees the shared kernel's point type.
 */
@Component
public class DeliveryAddressGeocodeAdapter implements GeocodeDeliveryAddressPort {

    private final GeocodingClient geocodingClient;

    public DeliveryAddressGeocodeAdapter(GeocodingClient geocodingClient) {
        this.geocodingClient = geocodingClient;
    }

    @Override
    public Optional<GeoLocation> locate(DeliveryAddress address) {
        return geocodingClient.locate(address.asSearchText())
                .map(point -> new GeoLocation(point.lat(), point.lng()));
    }
}
