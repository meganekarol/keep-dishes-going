package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;
import org.springframework.modulith.events.Externalized;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published when the kitchen has finished and a courier can collect (US-28).
 *
 * <p>Their side answers this with an {@code OrderPickedUpEvent} within a
 * minute.
 */
@Externalized("kdg.events::#{'restaurant.' + #this.restaurantId() + '.order.ready.v1'}")
public record OrderReadyForPickupEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID restaurantId,
        UUID orderId
) implements DomainEvent {

    public OrderReadyForPickupEvent(UUID orderId, UUID restaurantId) {
        this(UUID.randomUUID(), LocalDateTime.now(), restaurantId, orderId);
    }
}
