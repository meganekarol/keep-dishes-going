package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a customer places a new order
 * Internal event - NO @Externalized (stays within the system)
 */
public record OrderPlacedEvent(
    UUID eventId,
    LocalDateTime occurredOn,
    UUID orderId,
    UUID restaurantId,
    String customerName,
    BigDecimal totalPrice,
    String deliveryAddress
) implements DomainEvent {

    @Override
    public LocalDateTime occurredAt() {
        return null;
    }
}