package be.kdg.keepdishesgoing.order.port.out;

import java.util.UUID;

/**
 * Port for loading restaurant projections
 */
public interface LoadRestaurantProjectionPort {

    boolean isRestaurantOpen(UUID restaurantId);

    String getRestaurantName(UUID restaurantId);

    /**
     * Minutes the kitchen asks for, or {@code 0} when it was never recorded.
     * Feeds the delivery guess on the tracking page (US-19).
     */
    int getPreparationTime(UUID restaurantId);

    /**
     * Where a courier collects an order from, for the message that dispatches
     * one (US-28).
     *
     * @return the address, with {@code coordinates} left {@code null} when the
     *         restaurant was never placed on the map
     */
    PickupPoint getPickupPoint(UUID restaurantId);

    /** The restaurant's address as the delivery service wants to read it. */
    record PickupPoint(String street, String number, String postalCode, String city,
                       Double latitude, Double longitude) {

        public boolean hasCoordinates() {
            return latitude != null && longitude != null;
        }
    }
}