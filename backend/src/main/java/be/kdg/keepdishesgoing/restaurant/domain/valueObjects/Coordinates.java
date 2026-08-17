package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

/**
 * A point on the map, used to work out how far a courier has to drive (US-19).
 *
 * <p>Optional on an {@link Address}: geocoding is a call to somebody else's
 * service and it is allowed to fail. A restaurant with no coordinates still
 * works, it just cannot have its distance measured.
 */
public record Coordinates(double latitude, double longitude) {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public Coordinates {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got " + longitude);
        }
    }

    /**
     * Great-circle distance in kilometres.
     *
     * <p>Straight over the ground, not along the roads. Route lengths need a
     * routing service, and the number this feeds is called a guesstimate.
     */
    public double distanceInKmTo(Coordinates other) {
        double deltaLat = Math.toRadians(other.latitude - this.latitude);
        double deltaLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
