package be.kdg.keepdishesgoing.restaurant.port.out;

import be.kdg.keepdishesgoing.common.DomainEvent;

import java.util.List;

public interface RestaurantEventPublisherPort {
    void publish(DomainEvent event);
    void publishAll(List<DomainEvent> events);
}
