package be.kdg.keepdishesgoing.order.adapter.out.messaging;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.order.port.out.OrderEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter for publishing domain events using Spring's ApplicationEventPublisher
 * Events with @Externalized will be sent to RabbitMQ via Spring Modulith
 * Other events will be published internally via Spring's event bus
 */
@Component
public class OrderDomainEventPublisher implements OrderEventPublisherPort {
    
    private static final Logger log = LoggerFactory.getLogger(OrderDomainEventPublisher.class);
    
    private final ApplicationEventPublisher eventPublisher;
    
    public OrderDomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing event: {}", event.getClass().getSimpleName());
        eventPublisher.publishEvent(event);
    }
    
    @Override
    public void publishAll(List<DomainEvent> events) {
        log.debug("Publishing {} events", events.size());
        events.forEach(this::publish);
    }
}