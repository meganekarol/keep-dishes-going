package be.kdg.keepdishesgoing.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQToplogy {
    public static final String KDG_EXCHANGE = "kdg.events";

    // Queue for receiving delivery updates from external service
    public static final String DELIVERY_UPDATES_QUEUE = "keepdishesgoing.delivery-updates";

    /**
     * Topic exchange for all events.
     * Used by external delivery service to consume our order events
     * and to publish delivery updates back to us.
     */
    @Bean
    TopicExchange kdgEventsExchange() {
        return ExchangeBuilder
                .topicExchange(KDG_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== QUEUES FOR CONSUMING DELIVERY EVENTS ====================

    /**
     * Queue for receiving all delivery-related events from external service:
     * - Order picked up by courier
     * - Order delivered to customer
     * - Courier location updates
     */
    @Bean
    Queue deliveryUpdatesQueue() {
        return QueueBuilder
                .durable(DELIVERY_UPDATES_QUEUE)
                .build();
    }

    // ==================== BINDINGS FOR DELIVERY EVENTS (We consume) ====================

    @Bean
    Binding bindOrderPickedUp() {
        return BindingBuilder
                .bind(deliveryUpdatesQueue())
                .to(kdgEventsExchange())
                .with("delivery.*.order.pickedup.v1");
    }

    @Bean
    Binding bindOrderDelivered() {
        return BindingBuilder
                .bind(deliveryUpdatesQueue())
                .to(kdgEventsExchange())
                .with("delivery.*.order.delivered.v1");
    }

    @Bean
    Binding bindOrderLocation() {
        return BindingBuilder
                .bind(deliveryUpdatesQueue())
                .to(kdgEventsExchange())
                .with("delivery.*.order.location.v1");
    }

    /*
     * NOTE: We do NOT create queues for restaurant.*.order.accepted.v1 or
     * restaurant.*.order.ready.v1 because:
     *
     * 1. Those events are published BY US to RabbitMQ (via @Externalized)
     * 2. The EXTERNAL delivery service creates their own queues to consume them
     * 3. We don't need to consume our own events
     *
     * Spring Modulith with @Externalized automatically publishes these events
     * to the kdg.events exchange with the correct routing keys.
     */
}
