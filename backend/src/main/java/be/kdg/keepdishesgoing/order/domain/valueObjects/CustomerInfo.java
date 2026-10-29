package be.kdg.keepdishesgoing.order.domain.valueObjects;

import java.util.Objects;

/**
 * Who placed an order and how to reach them (US-22).
 *
 * @param customerName the name the order is under
 * @param contactEmail the address the confirmation goes to
 */
public record CustomerInfo(
        String customerName,
        String contactEmail
) {

    /**
     * Checks both details are present and the email looks like one.
     *
     * @throws NullPointerException     if either detail is missing
     * @throws IllegalArgumentException if either is blank, or the email has no
     *                                  {@code @} and dot
     */
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
        return email.contains("@") && email.contains(".");
    }
}
