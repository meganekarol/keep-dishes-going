package be.kdg.keepdishesgoing.order.adapter.in.web;

import be.kdg.keepdishesgoing.common.events.OrderDeliveredEvent;
import be.kdg.keepdishesgoing.common.events.OrderPickedUpEvent;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import be.kdg.keepdishesgoing.order.port.in.UpdateOrderStatusUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.UpdateOrderStatusCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ listener for events from the external delivery service
 * Handles delivery updates: picked up, location updates, and delivered
 */
@Component
public class DeliveryEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(DeliveryEventListener.class);
    
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    
    public DeliveryEventListener(UpdateOrderStatusUseCase updateOrderStatusUseCase) {
        this.updateOrderStatusUseCase = updateOrderStatusUseCase;
    }
    
    /**
     * Handle OrderPickedUpEvent from delivery service
     * Routing key: delivery.{orderId}.order.pickedup.v1
     */
    @RabbitListener(queues = "keepdishesgoing.delivery-updates")
    public void handleOrderPickedUp(OrderPickedUpEvent event) {
        log.info("Received OrderPickedUpEvent for order: {}", event.orderId());
        
        try {
            UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(
                event.orderId(),
                OrderStatus.IN_DELIVERY
            );
            
            updateOrderStatusUseCase.updateOrderStatus(command);
            log.info("Order {} status updated to IN_DELIVERY", event.orderId());
            
        } catch (Exception e) {
            log.error("Error processing OrderPickedUpEvent for order {}", event.orderId(), e);
            // In production: send to dead letter queue or retry
        }
    }
    
    /**
     * Handle OrderDeliveredEvent from delivery service
     * Routing key: delivery.{orderId}.order.delivered.v1
     */
    @RabbitListener(queues = "keepdishesgoing.delivery-updates")
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("Received OrderDeliveredEvent for order: {}", event.orderId());
        
        try {
            UpdateOrderStatusCommand command = new UpdateOrderStatusCommand(
                event.orderId(),
                OrderStatus.DELIVERED
            );
            
            updateOrderStatusUseCase.updateOrderStatus(command);
            log.info("Order {} status updated to DELIVERED", event.orderId());
            
        } catch (Exception e) {
            log.error("Error processing OrderDeliveredEvent for order {}", event.orderId(), e);

        }
    }

}