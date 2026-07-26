package be.kdg.keepdishesgoing.order.adapter.in.web;

import be.kdg.keepdishesgoing.order.domain.valueObjects.CustomerInfo;
import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderId;
import be.kdg.keepdishesgoing.order.port.in.commands.OrderItemCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;
import be.kdg.keepdishesgoing.order.port.in.PlaceOrderUseCase;
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
    
    public CustomerOrderController(PlaceOrderUseCase placeOrderUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
    }
    
    /**
     * Place a new order
     */
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("Received order placement request for restaurant {}", request.restaurantId);
        
        try {
            // Map request to command
            PlaceOrderCommand command = mapToCommand(request);
            
            // Execute use case
            OrderId orderId = placeOrderUseCase.placeOrder(command);
            
            log.info("Order placed successfully: {}", orderId);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PlaceOrderResponse(orderId.uuid(), "Order placed successfully"));
                
        } catch (IllegalStateException e) {
            log.warn("Order placement failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new PlaceOrderResponse(null, e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid order request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new PlaceOrderResponse(null, e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error placing order", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new PlaceOrderResponse(null, "Failed to place order"));
        }
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