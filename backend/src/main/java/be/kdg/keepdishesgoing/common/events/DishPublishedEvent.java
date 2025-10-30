package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Internal domain event - published when a dish becomes available to customers.
 * This event is consumed by the Order context to update its projection of available dishes.
 * NOT sent to RabbitMQ - only used for internal communication via ApplicationEventPublisher.
 */
public record DishPublishedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID dishId,
        UUID restaurantId,
        String dishName,
        String dishType,
        BigDecimal price,
        String description
) implements DomainEvent {
    
    public DishPublishedEvent(UUID dishId, UUID restaurantId, String dishName, 
                              String dishType, BigDecimal price, String description) {
        this(UUID.randomUUID(), LocalDateTime.now(), dishId, restaurantId, 
             dishName, dishType, price, description);
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}