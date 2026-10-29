package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * Where the food is going (US-22).
 *
 * @param street     the street name
 * @param number     the house number
 * @param postalCode the postal code
 * @param city       the city
 * @param country    the country
 * @param location   where the address sits on the map, or {@code null} if
 *                   geocoding it did not work. It is sent to the delivery
 *                   service as the dropoff point (US-28)
 */
public record DeliveryAddress(
        String street,
        String number,
        String postalCode,
        String city,
        String country,
        GeoLocation location
) {

    /** @throws IllegalArgumentException if the street, number, postal code or city is missing */
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

    /**
     * Builds an address as the customer typed it, before it has been geocoded.
     *
     * @param street     the street name
     * @param number     the house number
     * @param postalCode the postal code
     * @param city       the city
     * @param country    the country
     */
    public DeliveryAddress(String street, String number, String postalCode, String city, String country) {
        this(street, number, postalCode, city, country, null);
    }

    /**
     * Returns a copy of this address with a map location added.
     *
     * @param located the point the address was geocoded to
     * @return a new address carrying that location
     */
    public DeliveryAddress locatedAt(GeoLocation located) {
        return new DeliveryAddress(street, number, postalCode, city, country, located);
    }

    /**
     * Formats the address as one line, for a geocoding service to look up.
     *
     * @return the address as a single search string
     */
    public String asSearchText() {
        return "%s %s, %s %s, %s".formatted(street, number, postalCode, city, country);
    }

    /**
     * Formats the address for showing to a person.
     *
     * @return the whole address on one line
     */
    public String getFullAddress() {
        return String.format("%s %s, %s %s, %s",
                street, number, postalCode, city, country != null ? country : "");
    }
}
