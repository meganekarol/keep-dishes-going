package be.kdg.keepdishesgoing.common.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;

/**
 * Looks an address up with OpenStreetMap's Nominatim.
 * <p>Every failure is swallowed and logged. This runs inside transactions that
 * create restaurants and place orders, and neither should fail because a
 * public map server timed out.
 */
@Component
public class GeocodingClient {

    private static final Logger log = LoggerFactory.getLogger(GeocodingClient.class);

    private final RestClient restClient;
    private final boolean enabled;

    public GeocodingClient(
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
     * Short and deliberate. This sits on paths a user is waiting on, so a slow
     * lookup must give up quickly rather than hold the request open.
     */
    private static ClientHttpRequestFactory timeouts() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;
    }

    /**
     * @param searchText the address as one line, e.g. {@code Meir 12, 2000 Antwerpen, Belgium}
     * @return empty when the address could not be placed, which includes the
     *         service being unreachable
     */
    public Optional<GeoPoint> locate(String searchText) {
        if (!enabled) {
            log.debug("Geocoding switched off, not looking up {}", searchText);
            return Optional.empty();
        }

        try {
            // The client's own builder, which percent-encodes the query for us.
            // Hand-building the URI rejected the spaces in a street name.
            NominatimPlace[] places = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search")
                            .queryParam("q", searchText)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .body(NominatimPlace[].class);

            if (places == null || places.length == 0) {
                log.info("Nominatim found nothing for {}", searchText);
                return Optional.empty();
            }

            GeoPoint located = new GeoPoint(
                    Double.parseDouble(places[0].lat()),
                    Double.parseDouble(places[0].lon()));
            log.info("Placed {} at {}, {}", searchText, located.lat(), located.lng());
            return Optional.of(located);

        } catch (Exception e) {
            log.warn("Could not geocode {}: {}", searchText, e.getMessage());
            return Optional.empty();
        }
    }

    /** Nominatim returns latitude and longitude as strings. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimPlace(String lat, String lon) {
    }
}
