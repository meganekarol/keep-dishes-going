package be.kdg.keepdishesgoing.order.core;

import be.kdg.keepdishesgoing.order.domain.Order;
import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderLine;
import be.kdg.keepdishesgoing.order.domain.valueObjects.RestaurantId;
import be.kdg.keepdishesgoing.order.port.in.PlaceOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.commands.OrderItemCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;
import be.kdg.keepdishesgoing.order.port.out.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
    private final OrderEventPublisherPort publishEventPort;
    private final GeocodeDeliveryAddressPort geocodeDeliveryAddressPort;

    public PlaceOrderUseCaseImpl(
            LoadRestaurantProjectionPort loadRestaurantPort,
            LoadDishProjectionPort loadDishPort,
            SaveOrderPort updateOrderPort,
            OrderEventPublisherPort publishEventPort,
            GeocodeDeliveryAddressPort geocodeDeliveryAddressPort) {
        this.loadRestaurantPort = loadRestaurantPort;
        this.loadDishPort = loadDishPort;
        this.updateOrderPort = updateOrderPort;
        this.publishEventPort = publishEventPort;
        this.geocodeDeliveryAddressPort = geocodeDeliveryAddressPort;
    }
    
    @Override
    public OrderId placeOrder(PlaceOrderCommand command) {
        log.info("Placing order for restaurant: {}", command.restaurantId());
        
        // Validate restaurant is open
        validateRestaurantOpen(command.restaurantId());
        
        // Validate all dishes are available and belong to restaurant
        validateDishes(command.items(), command.restaurantId());

        // Resolve the requested items into priced order lines
        List<OrderLine> enrichedOrderLines = toOrderLines(command.items());

        DeliveryAddress deliveryAddress = geocodeDeliveryAddressPort.locate(command.deliveryAddress())
            .map(command.deliveryAddress()::locatedAt)
            .orElse(command.deliveryAddress());

        // Create Order aggregate
        OrderId orderId = OrderId.create();
        Order order = Order.placeOrder(
            orderId,
            command.customerInfo(),
            RestaurantId.of(command.restaurantId()),
            deliveryAddress,
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
    
    private void validateDishes(List<OrderItemCommand> items, UUID restaurantId) {
        List<UUID> dishIds = items.stream()
            .map(OrderItemCommand::dishId)
            .toList();

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
    
    /**
     * Turns the requested dish/quantity pairs into order lines, taking the name
     * and price from the dish projection. Prices are never accepted from the
     * client, and once the resulting event is stored the order total is fixed.
     */
    private List<OrderLine> toOrderLines(List<OrderItemCommand> items) {
        return items.stream()
            .map(item -> new OrderLine(
                item.dishId(),
                loadDishPort.getDishName(item.dishId()),
                loadDishPort.getDishPrice(item.dishId()),
                item.quantity()
            ))
            .toList();
    }
}