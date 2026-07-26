package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event published when a restaurant rejects an order
 * Internal event - NO @Externalized
 */
public record OrderRejectedEvent(
    UUID eventId,
    LocalDateTime occurredOn,
    UUID orderId,
    UUID restaurantId,
    String rejectionReason
) implements DomainEvent {

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}