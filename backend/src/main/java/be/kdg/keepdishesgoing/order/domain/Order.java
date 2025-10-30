package be.kdg.keepdishesgoing.order.domain;

import be.kdg.keepdishesgoing.common.DomainEvent;
import be.kdg.keepdishesgoing.common.events.*;
import be.kdg.keepdishesgoing.order.domain.valueObjects.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order Aggregate Root - Event Sourced with Snapshot support
 * Manages the complete lifecycle of a customer order
 */
public class Order {
    private OrderId orderId;
    private CustomerInfo customerInfo;
    private RestaurantId restaurantId;
    private DeliveryAddress deliveryAddress;
    private List<OrderLine> orderLines;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime placedAt;
    private LocalDateTime acceptedAt;
    private LocalDateTime readyAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private String rejectionReason;
    private int version;

    // For event sourcing
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    // Constructor for new orders
    private Order(OrderId orderId, CustomerInfo customerInfo,RestaurantId restaurantId,
                  DeliveryAddress deliveryAddress, List<OrderLine> orderLines, BigDecimal totalPrice) {
        this.orderId = orderId;
        this.customerInfo = customerInfo;
        this.restaurantId = restaurantId;
        this.deliveryAddress = deliveryAddress;
        this.orderLines = new ArrayList<>(orderLines);
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING_ACCEPTANCE;
        this.placedAt = LocalDateTime.now();
        this.version = 0;

        // Register domain event
        registerEvent(new OrderPlacedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                orderId.uuid(),
                restaurantId.uuid(),
                customerInfo.customerName(),
                totalPrice,
                deliveryAddress.getFullAddress()
        ));
    }

    // Constructor for event sourcing reconstruction
    private Order() {
        this.orderLines = new ArrayList<>();
    }

    // Factory method for placing new order
    public static Order placeOrder(OrderId orderId, CustomerInfo customerInfo,RestaurantId restaurantId,
                                    DeliveryAddress deliveryAddress,
                                   List<OrderLine> orderLines) {
        validateOrderLines(orderLines);
        BigDecimal totalPrice = calculateTotalPrice(orderLines);
        return new Order(orderId, customerInfo, restaurantId,deliveryAddress, orderLines, totalPrice);
    }

    // Factory method for snapshot restoration
    public static Order fromSnapshot(OrderSnapshot snapshot) {
        Order order = new Order();
        order.orderId = new OrderId(snapshot.orderId());
        order.customerInfo = snapshot.customerInfo();
        order.restaurantId= snapshot.restaurantId();
        order.deliveryAddress = snapshot.deliveryAddress();
        order.orderLines = new ArrayList<>(snapshot.orderLines());
        order.totalPrice = snapshot.totalPrice();
        order.status = snapshot.status();
        order.placedAt = snapshot.placedAt();
        order.acceptedAt = snapshot.acceptedAt();
        order.readyAt = snapshot.readyAt();
        order.pickedUpAt = snapshot.pickedUpAt();
        order.deliveredAt = snapshot.deliveredAt();
        order.rejectionReason = snapshot.rejectionReason();
        order.version = snapshot.version();
        return order;
    }

    // Business methods
    public void accept() {
        if (status != OrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order can only be accepted when pending acceptance");
        }

        this.status = OrderStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
        this.version++;

        registerEvent(new OrderAcceptedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                orderId.uuid(),
                restaurantId.uuid(),
                deliveryAddress.getFullAddress()
        ));
    }

    public void reject(String reason) {
        if (status != OrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order can only be rejected when pending acceptance");
        }

        this.status = OrderStatus.REJECTED;
        this.rejectionReason = reason;
        this.version++;

        registerEvent(new OrderRejectedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                orderId.uuid(),
                restaurantId.uuid(),
                reason
        ));
    }

    public void markReady() {
        if (status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order must be accepted before marking ready");
        }

        this.status = OrderStatus.READY_FOR_PICKUP;
        this.readyAt = LocalDateTime.now();
        this.version++;

        registerEvent(new OrderReadyEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                orderId.uuid(),
                restaurantId.uuid(),
                deliveryAddress.getFullAddress()
        ));
    }

    public void markPickedUp() {
        if (status != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException("Order must be ready before being picked up");
        }

        this.status = OrderStatus.IN_DELIVERY;
        this.pickedUpAt = LocalDateTime.now();
        this.version++;
    }

    public void markDelivered() {
        if (status != OrderStatus.IN_DELIVERY) {
            throw new IllegalStateException("Order must be in delivery before being delivered");
        }

        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.version++;
    }

    public void autoReject() {
        if (status != OrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order can only be auto-rejected when pending acceptance");
        }

        this.status = OrderStatus.REJECTED;
        this.rejectionReason = "No response from restaurant within time limit";
        this.version++;

        registerEvent(new OrderRejectedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                orderId.uuid(),
                restaurantId.uuid(),
                "Automatically rejected - no response within 5 minutes"
        ));
    }

    // Create snapshot for event sourcing
    public OrderSnapshot createSnapshot() {
        return new OrderSnapshot(
                orderId.uuid(),
                customerInfo,
                restaurantId,
                deliveryAddress,
                orderLines,
                totalPrice,
                status,
                placedAt,
                acceptedAt,
                readyAt,
                pickedUpAt,
                deliveredAt,
                rejectionReason,
                version
        );
    }

    // Helper methods
    private static void validateOrderLines(List<OrderLine> orderLines) {
        if (orderLines == null || orderLines.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one order line");
        }
    }

    private static BigDecimal calculateTotalPrice(List<OrderLine> orderLines) {
        return orderLines.stream()
                .map(OrderLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void registerEvent(DomainEvent event) {
        uncommittedEvents.add(event);
    }

    // Event sourcing support
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    // Getters
    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<OrderLine> getOrderLines() {
        return new ArrayList<>(orderLines);
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime getReadyAt() {
        return readyAt;
    }

    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public int getVersion() {
        return version;
    }

    public boolean isPendingAcceptance() {
        return status == OrderStatus.PENDING_ACCEPTANCE;
    }

    public boolean isAccepted() {
        return status == OrderStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == OrderStatus.REJECTED;
    }

    public boolean isReadyForPickup() {
        return status == OrderStatus.READY_FOR_PICKUP;
    }

    public boolean isInDelivery() {
        return status == OrderStatus.IN_DELIVERY;
    }

    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }
}