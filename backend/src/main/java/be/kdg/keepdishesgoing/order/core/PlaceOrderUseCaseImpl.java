package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.OrderId;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderLine;
import be.kdg.keepdishesgoing.order.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.order.port.in.PlaceOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;
import be.kdg.keepdishesgoing.order.port.out.LoadDishProjectionPort;
import be.kdg.keepdishesgoing.order.port.out.LoadRestaurantProjectionPort;
import be.kdg.keepdishesgoing.order.port.out.SaveOrderPort;
import be.kdg.keepdishesgoing.restaurant.port.out.PublishEventPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Use case implementation for placing a new order
 */
@Service
@Transactional
public class PlaceOrderUseCaseImpl implements PlaceOrderUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(PlaceOrderUseCaseImpl.class);
    
    private final LoadRestaurantProjectionPort loadRestaurantPort;
    private final LoadDishProjectionPort loadDishPort;
    private final SaveOrderPort updateOrderPort;
    private final PublishEventPort publishEventPort;
    
    public PlaceOrderUseCaseImpl(
            LoadRestaurantProjectionPort loadRestaurantPort,
            LoadDishProjectionPort loadDishPort,
            SaveOrderPort updateOrderPort,
            PublishEventPort publishEventPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.loadDishPort = loadDishPort;
        this.updateOrderPort = updateOrderPort;
        this.publishEventPort = publishEventPort;
    }
    
    @Override
    public OrderId placeOrder(PlaceOrderCommand command) {
        log.info("Placing order for restaurant: {}", command.restaurantId());
        
        // Validate restaurant is open
        validateRestaurantOpen(command.restaurantId());
        
        // Validate all dishes are available and belong to restaurant
        validateDishes(command.orderLines(), command.restaurantId());
        
        // Enrich order lines with current dish information
        List<OrderLine> enrichedOrderLines = enrichOrderLines(command.orderLines());

        // Create Order aggregate
        OrderId orderId = OrderId.create();
        Order order = Order.placeOrder(
            orderId,
            command.customerInfo(),
            RestaurantId.of(command.restaurantId()),
            command.deliveryAddress(),
            enrichedOrderLines
        );
        
        // 6. Save order events to event store
        updateOrderPort.save(order);
        
        // 7. Publish domain events
        publishEventPort.publishAll(order.getUncommittedEvents());
        order.clearUncommittedEvents();
        
        log.info("Order placed successfully: {}", orderId);
        return orderId;
    }
    
    private void validateRestaurantOpen(UUID restaurantId) {
        boolean isOpen = loadRestaurantPort.isRestaurantOpen(restaurantId);
        if (!isOpen) {
            log.warn("Restaurant {} is closed", restaurantId);
            throw new IllegalStateException("Restaurant is currently closed and cannot accept orders");
        }
    }
    
    private void validateDishes(List<OrderLine> orderLines, UUID restaurantId) {
        List<UUID> dishIds = orderLines.stream()
            .map(OrderLine::dishId)
            .collect(Collectors.toList());
        
        // Check all dishes are available
        boolean allAvailable = loadDishPort.areAllDishesAvailable(dishIds);
        if (!allAvailable) {
            log.warn("Some dishes are not available for restaurant {}", restaurantId);
            throw new IllegalStateException("One or more dishes are not available for ordering");
        }
        
        // Verify all dishes belong to the restaurant
        for (UUID dishId : dishIds) {
            if (!loadDishPort.dishBelongsToRestaurant(dishId, restaurantId)) {
                log.error("Dish {} does not belong to restaurant {}", dishId, restaurantId);
                throw new IllegalArgumentException("Dish does not belong to the selected restaurant");
            }
        }
    }
    
    private List<OrderLine> enrichOrderLines(List<OrderLine> orderLines) {
        // Enrich with current prices and names from projections
        return orderLines.stream()
            .map(line -> new OrderLine(
                line.dishId(),
                loadDishPort.getDishName(line.dishId()),
                loadDishPort.getDishPrice(line.dishId()),
                line.quantity()
            ))
            .collect(Collectors.toList());
    }
}