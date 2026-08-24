package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

/**
 * Where a restaurant is.
 *
 * @param street      the street name
 * @param number      the house number
 * @param postalCode  the postal code
 * @param city        the city
 * @param country     the country
 * @param coordinates where the address sits on the map, or {@code null} if
 *                    geocoding it did not work
 */
public record Address(
        String street,
        int number,
        String postalCode,
        String city,
        String country,
        Coordinates coordinates) {

    /**
     * Builds an address as the owner typed it, before it has been geocoded.
     *
     * @param street     the street name
     * @param number     the house number
     * @param postalCode the postal code
     * @param city       the city
     * @param country    the country
     */
    public Address(String street, int number, String postalCode, String city, String country) {
        this(street, number, postalCode, city, country, null);
    }

    /**
     * Returns a copy of this address with coordinates added.
     *
     * @param located the point the address was geocoded to
     * @return a new address carrying those coordinates
     */
    public Address locatedAt(Coordinates located) {
        return new Address(street, number, postalCode, city, country, located);
    }

    /**
     * Formats the address as one line, for a geocoding service to look up.
     *
     * @return the address as a single search string
     */
    public String asSearchText() {
        return "%s %d, %s %s, %s".formatted(street, number, postalCode, city, country);
    }
}
