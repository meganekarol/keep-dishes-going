package be.kdg.keepdishesgoing.order.port.in.commands;
;
import java.util.UUID;

/**
 * Command for marking an accepted order as ready for pickup.
 */
public record MarkOrderReadyCommand(
        UUID orderId,
        UUID restaurantId
) {

    public MarkOrderReadyCommand {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurant ID cannot be null");
        }
    }
}