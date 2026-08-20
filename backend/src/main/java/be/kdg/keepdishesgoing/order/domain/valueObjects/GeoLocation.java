package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * A point on the map in the order context.
 */
public record GeoLocation(double lat, double lng) {

    public GeoLocation {
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got " + lat);
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got " + lng);
        }
    }
}
