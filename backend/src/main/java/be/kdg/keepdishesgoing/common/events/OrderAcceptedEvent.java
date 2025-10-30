package be.kdg.keepdishesgoing.common.events;

import be.kdg.keepdishesgoing.common.DomainEvent;
import org.springframework.modulith.events.Externalized;

import java.time.LocalDateTime;
import java.util.UUID;

@Externalized("kdg.events::#{'restaurant.' + #this.restaurantId() + '.order.accepted.v1'}")
public record OrderAcceptedEvent(
        UUID eventId,
        LocalDateTime occurredOn,
        UUID orderId,
        UUID restaurantId,
        String deliveryAddress
) implements DomainEvent {

    public OrderAcceptedEvent(UUID orderId, UUID restaurantId, String deliveryAddress) {
        this(UUID.randomUUID(), LocalDateTime.now(), orderId, restaurantId,
                deliveryAddress);
    }

    @Override
    public LocalDateTime occurredAt() {
        return occurredOn;
    }
}
