package be.kdg.keepdishesgoing.order.port.out;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * One stored order row, as the driven side hands it over.
 *
 * <p>Separate from {@code RestaurantOrderSummary}, which the inbound port owns.
 * The two carry the same fields today, but they answer to different sides: this
 * one follows the read model's columns, and the other is the contract the API
 * publishes. Merging them would let a change of storage reach the clients.
 */
public record OrderSummaryData(
        UUID orderId,
        // Carried here but not on the inbound record: a rebuild has to write
        // this column, while a caller asking for one restaurant's orders
        // already knows whose they are.
        UUID restaurantId,
        OrderStatus status,
        String customerName,
        String deliveryAddress,
        String itemSummary,
        int itemCount,
        BigDecimal totalPrice,
        LocalDateTime placedAt,
        /**
         * When the order reached its restaurant, which is when it was paid for
         * (US-23). Null while the customer is still at the payment provider.
         */
        LocalDateTime pendingSince,
        LocalDateTime acceptedAt,
        LocalDateTime readyAt,
        String rejectionReason
) {

    /** A dish and how many of it, whatever the caller read that from. */
    public record ItemLine(String dishName, int quantity) {
    }

    /**
     * The one place the two texts below are formatted.
     *
     * <p>Rows are written from two directions: the listener builds them from
     * the event as an order happens, and a rebuild builds them from the stored
     * aggregate. If each spelled the summary out for itself, a rebuild could
     * quietly reword every row in the table.
     */
    public static String describeItems(List<ItemLine> lines) {
        return lines.stream()
                .map(line -> line.quantity() + "x " + line.dishName())
                .collect(Collectors.joining(", "));
    }

    public static String describeAddress(String street, String number, String postalCode, String city) {
        return "%s %s, %s %s".formatted(street, number, postalCode, city);
    }

    public static int countItems(List<ItemLine> lines) {
        return lines.stream().mapToInt(ItemLine::quantity).sum();
    }
}
