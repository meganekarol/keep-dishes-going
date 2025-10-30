package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.util.Objects;

/**
 * Value Object representing customer information for an order
 */
public record CustomerInfo(
        String customerName,
        String contactEmail
) {

    public CustomerInfo {
        Objects.requireNonNull(customerName, "Customer name cannot be null");
        Objects.requireNonNull(contactEmail, "Contact email cannot be null");

        if (customerName.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be blank");
        }

        if (contactEmail.isBlank()) {
            throw new IllegalArgumentException("Contact email cannot be blank");
        }

        if (!isValidEmail(contactEmail)) {
            throw new IllegalArgumentException("Invalid email format: " + contactEmail);
        }
    }

    private static boolean isValidEmail(String email) {
        // Simple email validation
        return email.contains("@") && email.contains(".");
    }
}
