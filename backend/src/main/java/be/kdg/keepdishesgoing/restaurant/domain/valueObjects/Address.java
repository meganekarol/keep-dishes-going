package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

/**
 * @param coordinates filled in by geocoding the rest of the address, and
 *                    {@code null} when that did not work. Only the delivery
 *                    guesstimate reads it, so an address without it is still a
 *                    perfectly good address.
 */
public record Address(
        String street,
        int number,
        String postalCode,
        String city,
        String country,
        Coordinates coordinates) {

    /** An address as the owner typed it, before anyone has looked it up. */
    public Address(String street, int number, String postalCode, String city, String country) {
        this(street, number, postalCode, city, country, null);
    }

    public Address locatedAt(Coordinates located) {
        return new Address(street, number, postalCode, city, country, located);
    }

    /** What a geocoding service is asked to find. */
    public String asSearchText() {
        return "%s %d, %s %s, %s".formatted(street, number, postalCode, city, country);
    }
}
