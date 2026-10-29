package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * A point on the map, used for the delivery address and the courier's position.
 *
 * @param lat the latitude, between -90 and 90
 * @param lng the longitude, between -180 and 180
 */
public record GeoLocation(double lat, double lng) {

    /** @throws IllegalArgumentException if either value is out of range */
    public GeoLocation {
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got " + lat);
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got " + lng);
        }
    }
}
