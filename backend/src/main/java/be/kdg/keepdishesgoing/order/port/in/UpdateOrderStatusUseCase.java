package be.kdg.keepdishesgoing.order.port.in;

import be.kdg.keepdishesgoing.order.port.in.commands.UpdateOrderStatusCommand;

/**
 * Use case interface for updating order status based on delivery events
 */
public interface UpdateOrderStatusUseCase {
    
    /**
     * Update order status (triggered by delivery service events)
     * 
     * @param command the command containing order ID and new status
     * @throws IllegalStateException if status transition is invalid
     */
    void updateOrderStatus(UpdateOrderStatusCommand command);
}