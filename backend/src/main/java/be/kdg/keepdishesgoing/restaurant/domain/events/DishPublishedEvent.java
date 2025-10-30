package be.kdg.keepdishesgoing.restaurant.domain.events;

import be.kdg.keepdishesgoing.common.DomainEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


public record DishPublishedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID dishId,
        UUID restaurantId,
        String dishName,
        BigDecimal price
) implements DomainEvent {
    public DishPublishedEvent(UUID dishId, UUID restaurantId, String dishName, BigDecimal price) {
        this(UUID.randomUUID(), LocalDateTime.now(), dishId, restaurantId, dishName, price);
    }

    @Override
    public LocalDateTime occurredAt() {
        return null;
    }
}
