package be.kdg.keepdishesgoing.common.geocoding;

/**
 * A point on the map, in the shared kernel because both contexts need one.
 *
 * <p>The restaurant context places a restaurant so the catalogue can measure
 * distance (US-19); the order context places a delivery address because the
 * delivery service is sent pickup and dropoff coordinates (US-28). Each keeps
 * its own value object; this is only what the geocoding client hands back.
 */
public record GeoPoint(double lat, double lng) {
}
