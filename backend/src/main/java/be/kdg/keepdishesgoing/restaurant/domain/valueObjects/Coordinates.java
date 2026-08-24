package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

/**
 * A point on the map, used to work out how far a courier has to drive (US-19).
 *
 * @param latitude  the latitude, between -90 and 90
 * @param longitude the longitude, between -180 and 180
 */
public record Coordinates(double latitude, double longitude) {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Checks the point is on the globe.
     *
     * @throws IllegalArgumentException if either value is out of range
     */
    public Coordinates {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90, got " + latitude);
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180, got " + longitude);
        }
    }

    /**
     * Measures the distance to another point in a straight line over the ground.
     *
     * <p>This is not the length of the roads, which is why the delivery time it
     * feeds is called a guesstimate.
     *
     * @param other the point to measure to
     * @return the distance in kilometres
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
