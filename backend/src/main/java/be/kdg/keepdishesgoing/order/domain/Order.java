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
 * Order Aggregate Root - Event Sourced with Snapshot support.
 * Manages the complete lifecycle of a customer order.
 *
 * <p>Event sourcing model:
 * <ul>
 *   <li>Business methods (accept, reject, ...) validate invariants, then
 *       {@link #applyAndRecord(DomainEvent)} the resulting event: the event both
 *       mutates state (via {@link #apply(DomainEvent)}) and is queued as
 *       uncommitted so the persistence adapter can append it to the event store.</li>
 *   <li>To rehydrate, the persistence adapter starts from {@link #blank()} (or a
 *       snapshot) and replays historical events with {@link #apply(DomainEvent)},
 *       which mutates state WITHOUT re-recording the events.</li>
 * </ul>
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

    // Constructor for event sourcing reconstruction (blank aggregate)
    private Order() {
        this.orderLines = new ArrayList<>();
    }

    /**
     * Blank aggregate used as the starting point for event replay.
     */
    public static Order blank() {
        return new Order();
    }

    // Factory method for placing a new order
    public static Order placeOrder(OrderId orderId, CustomerInfo customerInfo, RestaurantId restaurantId,
                                   DeliveryAddress deliveryAddress,
                                   List<OrderLine> orderLines) {
        validateOrderLines(orderLines);
        BigDecimal totalPrice = calculateTotalPrice(orderLines);

        List<OrderPlacedEvent.LineItem> lineItems = orderLines.stream()
                .map(ol -> new OrderPlacedEvent.LineItem(
                        ol.dishId(), ol.dishName(), ol.unitPrice(), ol.quantity()))
                .toList();

        Order order = new Order();
        order.applyAndRecord(new OrderPlacedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                orderId.uuid(),
                restaurantId.uuid(),
                customerInfo.customerName(),
                customerInfo.contactEmail(),
                deliveryAddress.street(),
                deliveryAddress.number(),
                deliveryAddress.postalCode(),
                deliveryAddress.city(),
                deliveryAddress.country(),
                lineItems,
                totalPrice
        ));
        return order;
    }

    // Factory method for snapshot restoration
    public static Order fromSnapshot(OrderSnapshot snapshot) {
        Order order = new Order();
        order.orderId = new OrderId(snapshot.orderId());
        order.customerInfo = snapshot.customerInfo();
        order.restaurantId = snapshot.restaurantId();
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

    // ==================== Business methods (command side) ====================

    public void accept() {
        if (status != OrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order can only be accepted when pending acceptance");
        }
        applyAndRecord(new OrderAcceptedEvent(
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
        applyAndRecord(new OrderRejectedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                orderId.uuid(),
                restaurantId.uuid(),
                reason
        ));
    }

    public void autoReject() {
        if (status != OrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order can only be auto-rejected when pending acceptance");
        }
        applyAndRecord(new OrderRejectedEvent(
                UUID.randomUUID(),
                LocalDateTime.now(),
                orderId.uuid(),
                restaurantId.uuid(),
                "Automatically rejected - no response within 5 minutes"
        ));
    }

    public void markReady() {
        if (status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order must be accepted before marking ready");
        }
        applyAndRecord(new OrderReadyEvent(
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
        applyAndRecord(new OrderPickedUpEvent(orderId.uuid(), null, null));
    }

    public void markDelivered() {
        if (status != OrderStatus.IN_DELIVERY) {
            throw new IllegalStateException("Order must be in delivery before being delivered");
        }
        applyAndRecord(new OrderDeliveredEvent(orderId.uuid(), LocalDateTime.now()));
    }

    // ==================== Event sourcing (state mutation) ====================

    /**
     * Applies an event to mutate aggregate state WITHOUT recording it.
     * Used by the persistence adapter to replay historical events during rehydration.
     */
    public void apply(DomainEvent event) {
        if (event instanceof OrderPlacedEvent e) {
            this.orderId = OrderId.of(e.orderId());
            this.restaurantId = RestaurantId.of(e.restaurantId());
            this.customerInfo = new CustomerInfo(e.customerName(), e.contactEmail());
            this.deliveryAddress = new DeliveryAddress(
                    e.street(), e.number(), e.postalCode(), e.city(), e.country());
            this.orderLines = new ArrayList<>(e.lines().stream()
                    .map(li -> new OrderLine(li.dishId(), li.dishName(), li.unitPrice(), li.quantity()))
                    .toList());
            this.totalPrice = e.totalPrice();
            this.status = OrderStatus.PENDING_ACCEPTANCE;
            this.placedAt = e.occurredOn();
            this.version = 0;
        } else if (event instanceof OrderAcceptedEvent e) {
            this.status = OrderStatus.ACCEPTED;
            this.acceptedAt = e.occurredOn();
            this.version++;
        } else if (event instanceof OrderRejectedEvent e) {
            this.status = OrderStatus.REJECTED;
            this.rejectionReason = e.rejectionReason();
            this.version++;
        } else if (event instanceof OrderReadyEvent e) {
            this.status = OrderStatus.READY_FOR_PICKUP;
            this.readyAt = e.occurredOn();
            this.version++;
        } else if (event instanceof OrderPickedUpEvent e) {
            this.status = OrderStatus.IN_DELIVERY;
            this.pickedUpAt = e.occurredOn();
            this.version++;
        } else if (event instanceof OrderDeliveredEvent e) {
            this.status = OrderStatus.DELIVERED;
            this.deliveredAt = e.occurredOn();
            this.version++;
        } else {
            throw new IllegalStateException("Unknown order event type: " + event.getClass().getName());
        }
    }

    private void applyAndRecord(DomainEvent event) {
        apply(event);
        uncommittedEvents.add(event);
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