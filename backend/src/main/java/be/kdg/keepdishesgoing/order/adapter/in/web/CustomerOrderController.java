package be.kdg.keepdishesgoing.order.adapter.in.web;

import be.kdg.keepdishesgoing.order.domain.valueObjects.CustomerInfo;
import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.commands.OrderItemCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;
import be.kdg.keepdishesgoing.order.port.in.PlaceOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.TrackOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.queries.OrderTracking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for customer-facing order operations
 */
@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerOrderController.class);
    
    private final PlaceOrderUseCase placeOrderUseCase;
    private final TrackOrderUseCase trackOrderUseCase;

    public CustomerOrderController(PlaceOrderUseCase placeOrderUseCase, TrackOrderUseCase trackOrderUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.trackOrderUseCase = trackOrderUseCase;
    }

    /**
     * Place a new order
     */
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("Received order placement request for restaurant {}", request.restaurantId);

        PlaceOrderCommand command = mapToCommand(request);
        OrderId orderId = placeOrderUseCase.placeOrder(command);

        log.info("Order placed successfully: {}", orderId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new PlaceOrderResponse(orderId.uuid(), "Order placed successfully"));
    }
    
    /**
     * Follow an order from placement to delivery (US-24, US-25).
     *
     * <p>The returned id from {@code POST /api/orders} is what the customer
     * tracks with — it is the confirmation and the tracking handle in one.
     * Intended to be polled while the order is in progress.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderTracking> trackOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(trackOrderUseCase.track(orderId));
    }

    private PlaceOrderCommand mapToCommand(PlaceOrderRequest request) {
        CustomerInfo customerInfo = new CustomerInfo(
            request.customerName,
            request.customerEmail
        );
        
        DeliveryAddress deliveryAddress = new DeliveryAddress(
            request.deliveryStreet,
            request.deliveryNumber,
            request.deliveryPostalCode,
            request.deliveryCity,
            request.deliveryCountry
        );
        
        // Only dish + quantity: the use case resolves name and price from the
        // dish projection so the client cannot influence what the order costs.
        List<OrderItemCommand> items = request.items.stream()
            .map(item -> new OrderItemCommand(item.dishId, item.quantity))
            .toList();

        return new PlaceOrderCommand(
            request.restaurantId,
            customerInfo,
            deliveryAddress,
            items
        );
    }
    
    // DTOs
    public record PlaceOrderRequest(
        UUID restaurantId,
        String customerName,
        String customerEmail,
        String deliveryStreet,
        String deliveryNumber,
        String deliveryPostalCode,
        String deliveryCity,
        String deliveryCountry,
        List<OrderItemRequest> items
    ) {}
    
    public record OrderItemRequest(
        UUID dishId,
        int quantity
    ) {}
    
    public record PlaceOrderResponse(
        UUID orderId,
        String message
    ) {}
}