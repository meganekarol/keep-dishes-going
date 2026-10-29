package be.kdg.keepdishesgoing.restaurant.adapter.out.messaging;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.restaurant.port.out.RestaurantEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestaurantDomainEventPublisher implements RestaurantEventPublisherPort {

    private final ApplicationEventPublisher eventPublisher;

    public RestaurantDomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        eventPublisher.publishEvent(event);
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
