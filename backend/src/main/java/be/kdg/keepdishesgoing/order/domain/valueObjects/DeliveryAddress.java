package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * Where the food is going.
 *
 * @param location filled in by geocoding the rest of the address, and
 *                 {@code null} when that did not work. The delivery service is
 *                 sent it as the dropoff point (US-28), so an order without it
 *                 can still be cooked but leaves the courier to find the door
 *                 from the written address alone.
 */
public record DeliveryAddress(
        String street,
        String number,
        String postalCode,
        String city,
        String country,
        GeoLocation location
) {
    public DeliveryAddress {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street is required");
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Number is required");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("Postal code is required");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City is required");
        }
    }

    /** An address as the customer typed it, before anyone has looked it up. */
    public DeliveryAddress(String street, String number, String postalCode, String city, String country) {
        this(street, number, postalCode, city, country, null);
    }

    public DeliveryAddress locatedAt(GeoLocation located) {
        return new DeliveryAddress(street, number, postalCode, city, country, located);
    }

    /** What a geocoding service is asked to find. */
    public String asSearchText() {
        return "%s %s, %s %s, %s".formatted(street, number, postalCode, city, country);
    }

    public String getFullAddress() {
        return String.format("%s %s, %s %s, %s",
                street, number, postalCode, city, country != null ? country : "");
    }
}
