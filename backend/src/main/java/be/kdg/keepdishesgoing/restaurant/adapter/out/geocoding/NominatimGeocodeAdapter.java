package be.kdg.keepdishesgoing.restaurant.adapter.out.geocoding;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Coordinates;
import be.kdg.keepdishesgoing.restaurant.port.out.GeocodeAddressPort;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;

/**
 * Looks an address up with OpenStreetMap's Nominatim.
 *
 * <p>Chosen because it needs no account. In return it asks for a real
 * User-Agent and no more than one call a second, which suits us: we geocode
 * once, when a restaurant is created.
 *
 * <p>Every failure is swallowed and logged. This runs inside the transaction
 * that creates a restaurant, and an owner should not lose their registration
 * because a public map server timed out.
 */
@Component
public class NominatimGeocodeAdapter implements GeocodeAddressPort {

    private static final Logger log = LoggerFactory.getLogger(NominatimGeocodeAdapter.class);

    private final RestClient restClient;
    private final boolean enabled;

    public NominatimGeocodeAdapter(
            @Value("${kdg.geocoding.base-url:https://nominatim.openstreetmap.org}") String baseUrl,
            @Value("${kdg.geocoding.enabled:true}") boolean enabled) {
        this.enabled = enabled;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                // Nominatim rejects callers that do not identify themselves.
                .defaultHeader("User-Agent", "KeepDishesGoing/1.0 (KdG Programming 6 project)")
                .requestFactory(timeouts())
                .build();
    }

    /**
     * Short and deliberate. This sits on the create-restaurant path, so a slow
     * lookup must give up quickly rather than hold the request open.
     */
    private static org.springframework.http.client.ClientHttpRequestFactory timeouts() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;
    }

    @Override
    public Optional<Coordinates> locate(Address address) {
        if (!enabled) {
            log.debug("Geocoding switched off, leaving {} without coordinates", address.city());
            return Optional.empty();
        }

        try {
            // The client's own builder, which percent-encodes the query for us.
            // Hand-building the URI rejected the spaces in the street name.
            NominatimPlace[] places = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("q", address.asSearchText())
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(NominatimPlace[].class);

            if (places == null || places.length == 0) {
                log.info("Nominatim found nothing for {}", address.asSearchText());
                return Optional.empty();
            }

            Coordinates located = new Coordinates(
                    Double.parseDouble(places[0].lat()),
                    Double.parseDouble(places[0].lon()));
            log.info("Placed {} at {}, {}", address.city(), located.latitude(), located.longitude());
            return Optional.of(located);

        } catch (Exception e) {
            log.warn("Could not geocode {}: {}", address.asSearchText(), e.getMessage());
            return Optional.empty();
        }
    }

    /** Nominatim returns latitude and longitude as strings. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimPlace(String lat, String lon) {
    }
}
