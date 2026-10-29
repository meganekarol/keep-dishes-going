package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import be.kdg.keepdishesgoing.order.port.in.UpdateOrderStatusUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.UpdateOrderStatusCommand;
import be.kdg.keepdishesgoing.order.port.out.LoadOrderPort;
import be.kdg.keepdishesgoing.order.port.out.OrderEventPublisherPort;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case implementation for updating order status based on delivery service events
 */
@Service
@Transactional
public class UpdateOrderStatusUseCaseImpl implements UpdateOrderStatusUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(UpdateOrderStatusUseCaseImpl.class);
    
    private final LoadOrderPort loadOrderPort;
    private final SaveOrderPort updateOrderPort;
    private final OrderEventPublisherPort publishEventPort;
    
    public UpdateOrderStatusUseCaseImpl(
            LoadOrderPort loadOrderPort,
            SaveOrderPort updateOrderPort,
            OrderEventPublisherPort publishEventPort) {
        this.loadOrderPort = loadOrderPort;
        this.updateOrderPort = updateOrderPort;
        this.publishEventPort = publishEventPort;
    }
    
    @Override
    public void updateOrderStatus(UpdateOrderStatusCommand command) {
        log.info("Updating order {} to status {}", command.orderId(), command.newStatus());
        
        // 1. Load order from event store
        OrderId orderId = OrderId.of(command.orderId());
        Order order = loadOrderPort.loadBy(orderId).orElseThrow();
        
        // 2. Update status based on the new status
        updateStatus(order, command.newStatus());
        
        // 3. Save events
        updateOrderPort.save(order);
        
        // 4. Create snapshot if order is delivered (final state)
        if (command.newStatus() == OrderStatus.DELIVERED) {
            updateOrderPort.saveSnapshot(order);
        }
        
        // 5. Publish events (if any were generated)
        if (!order.getUncommittedEvents().isEmpty()) {
            publishEventPort.publishAll(order.getUncommittedEvents());
            order.clearUncommittedEvents();
        }
        
        log.info("Order {} status updated to {}", command.orderId(), command.newStatus());
    }
    
    /**
     * Moves the order along, filling in a step the delivery service skipped.
     *
     * <p>Their specification says an {@code OrderPickedUpEvent} follows the
     * ready event within a minute. The running service never sends one: a spy
     * queue bound to every routing key on the exchange saw only location and
     * delivered messages across a full run. So an order would sit at
     * "waiting for a courier" until a delivered message arrived and was
     * refused, because the aggregate rightly will not deliver what was never
     * picked up.
     *
     * <p>Rather than weaken that rule, the missing step is recorded here. Food
     * that has been delivered was necessarily collected first, so writing both
     * events is the truth, not a workaround.
     */
    private void updateStatus(Order order, OrderStatus newStatus) {
        switch (newStatus) {
            case IN_DELIVERY:
                // A second picked-up message, or one arriving after we inferred
                // the pickup ourselves, must not fail the whole delivery.
                if (order.isReadyForPickup()) {
                    order.markPickedUp();
                } else {
                    log.debug("Order {} is already past pickup, leaving it at {}",
                            order.getOrderId(), order.getStatus());
                }
                break;
            case DELIVERED:
                if (order.isReadyForPickup()) {
                    log.info("Order {} was delivered without a pickup message, recording the pickup too",
                            order.getOrderId());
                    order.markPickedUp();
                }
                order.markDelivered();
                break;
            default:
                log.warn("Attempted to update order {} to unexpected status {}", 
                    order.getOrderId(), newStatus);
                throw new IllegalArgumentException(
                    "Invalid status update: " + newStatus + ". Only IN_DELIVERY and DELIVERED are allowed.");
        }
    }
}