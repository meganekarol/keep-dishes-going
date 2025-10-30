package be.kdg.keepdishesgoing.order.domain.valueObjects;

/**
 * Value object representing Delivery Address
 */
public record DeliveryAddress(
        String street,
        String number,
        String postalCode,
        String city,
        String country
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

    public String getFullAddress() {
        return String.format("%s %s, %s %s, %s",
                street, number, postalCode, city, country != null ? country : "");
    }
}
