package be.kdg.keepdishesgoing.order.adapter.in.web;

import be.kdg.keepdishesgoing.order.domain.valueObjects.CustomerInfo;
import be.kdg.keepdishesgoing.order.domain.valueObjects.DeliveryAddress;
import be.kdg.keepdishesgoing.order.port.in.commands.OrderItemCommand;
import be.kdg.keepdishesgoing.order.port.in.commands.PlaceOrderCommand;
import be.kdg.keepdishesgoing.order.port.in.PlaceOrderUseCase;
import be.kdg.keepdishesgoing.order.port.in.TrackOrderQuery;
import be.kdg.keepdishesgoing.order.port.in.queries.Checkout;
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
    private final TrackOrderQuery trackOrderQuery;

    public CustomerOrderController(PlaceOrderUseCase placeOrderUseCase, TrackOrderQuery trackOrderQuery) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.trackOrderQuery = trackOrderQuery;
    }

    /**
     * Place a new order and open its payment (US-22, US-23).
     *
     * <p>Answers with the order id and a payment page to continue at. The id is
     * handed over before the customer leaves to pay, so an order survives them
     * abandoning the payment: they can still look it up and see it was never
     * charged.
     */
    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@RequestBody PlaceOrderRequest request) {
        log.info("Received order placement request for restaurant {}", request.restaurantId);

        PlaceOrderCommand command = mapToCommand(request);
        Checkout checkout = placeOrderUseCase.placeOrder(command);

        log.info("Order {} placed, sending the customer to pay", checkout.orderId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new PlaceOrderResponse(checkout.orderId(), checkout.paymentUrl(), "Order placed, payment required"));
    }
    
    /**
     * Follow an order from placement to delivery (US-24, US-25).
     *
     * <p>The returned id from {@code POST /api/orders} is what the customer
     * tracks with: it is the confirmation and the tracking handle in one.
     * Intended to be polled while the order is in progress.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderTracking> trackOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(trackOrderQuery.track(orderId));
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
    
    /**
     * @param paymentUrl the provider's payment page. The customer has to be sent
     *                   there for the order to reach its restaurant at all.
     */
    public record PlaceOrderResponse(
        UUID orderId,
        String paymentUrl,
        String message
    ) {}
}