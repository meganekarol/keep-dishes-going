package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

public record Address(
        String street,
        String number,
        String postalCode, String city, String country) {
}
