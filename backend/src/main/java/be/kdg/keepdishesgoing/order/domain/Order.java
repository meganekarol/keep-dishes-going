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
    private Payment payment;
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
                deliveryAddress.location() == null ? null : deliveryAddress.location().lat(),
                deliveryAddress.location() == null ? null : deliveryAddress.location().lng(),
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
        order.payment = snapshot.payment();
        order.version = snapshot.version();
        return order;
    }

    // ==================== Business methods (command side) ====================

    /**
     * Records that the provider is holding the money, which is what makes the
     * order visible to its restaurant (US-23).
     *
     * <p>A repeat of a hold already recorded is ignored, since callbacks arrive
     * more than once.
     *
     * @param paymentReference the provider's id for the payment being held
     * @return whether this call changed anything
     */
    public boolean authorizePayment(String paymentReference) {
        if (payment.isHeld() && paymentReference.equals(payment.reference())) {
            return false;
        }
        if (status != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("Only an order awaiting payment can be paid for");
        }
        applyAndRecord(new OrderPaymentAuthorizedEvent(
                orderId.uuid(), restaurantId.uuid(), paymentReference, totalPrice));
        return true;
    }

    /**
     * Records the charge the provider has confirmed, as the restaurant accepts
     * (US-23).
     */
    public void recordPaymentCaptured() {
        if (!payment.isHeld()) {
            throw new IllegalStateException("There is no held payment to take");
        }
        applyAndRecord(new OrderPaymentCapturedEvent(
                orderId.uuid(), restaurantId.uuid(), payment.reference(), totalPrice));
    }

    /** Gives the hold back, for an order no kitchen is going to cook (US-23). */
    public void recordPaymentReleased() {
        if (!payment.isHeld()) {
            throw new IllegalStateException("There is no held payment to give back");
        }
        applyAndRecord(new OrderPaymentReleasedEvent(
                orderId.uuid(), restaurantId.uuid(), payment.reference()));
    }

    /** Abandons a checkout the customer walked away from (US-23). */
    public void expirePayment() {
        if (status != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("Only an order awaiting payment can expire");
        }
        applyAndRecord(new OrderPaymentExpiredEvent(orderId.uuid(), restaurantId.uuid()));
    }

    /**
     * Throws unless the kitchen may take this order on. Asked before capturing,
     * which no rollback can undo.
     */
    public void ensureAcceptable() {
        if (status != OrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order can only be accepted when pending acceptance");
        }
        if (!payment.isHeld()) {
            throw new IllegalStateException("Order has no held payment to take");
        }
    }

    /**
     * Takes the order on, and dispatches a courier by doing so (US-28).
     *
     * <p>The pickup half is passed in because an order knows which restaurant
     * it belongs to but not where that restaurant is. The dropoff half it has,
     * from the address the customer gave at checkout.
     *
     * @param pickupAddress     the restaurant's address
     * @param pickupCoordinates where the restaurant is, or {@code null} when it
     *                          was never geocoded
     */
    public void accept(OrderAcceptedEvent.Address pickupAddress,
                       OrderAcceptedEvent.Point pickupCoordinates) {
        if (status != OrderStatus.PENDING_ACCEPTANCE) {
            throw new IllegalStateException("Order can only be accepted when pending acceptance");
        }
        if (!payment.isCaptured()) {
            throw new IllegalStateException("Order cannot be accepted before its payment is taken");
        }
        applyAndRecord(new OrderAcceptedEvent(
                orderId.uuid(),
                restaurantId.uuid(),
                pickupAddress,
                pickupCoordinates,
                new OrderAcceptedEvent.Address(
                        deliveryAddress.street(),
                        deliveryAddress.number(),
                        deliveryAddress.postalCode(),
                        deliveryAddress.city()),
                dropoffCoordinates()
        ));
    }

    private OrderAcceptedEvent.Point dropoffCoordinates() {
        GeoLocation located = deliveryAddress.location();
        return located == null ? null : new OrderAcceptedEvent.Point(located.lat(), located.lng());
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
        applyAndRecord(new OrderReadyForPickupEvent(orderId.uuid(), restaurantId.uuid()));
    }

    public void markPickedUp() {
        if (status != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException("Order must be ready before being picked up");
        }
        applyAndRecord(new OrderPickedUpEvent(orderId.uuid(), restaurantId.uuid()));
    }

    public void markDelivered() {
        if (status != OrderStatus.IN_DELIVERY) {
            throw new IllegalStateException("Order must be in delivery before being delivered");
        }
        applyAndRecord(new OrderDeliveredEvent(orderId.uuid(), restaurantId.uuid()));
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
                    e.street(), e.number(), e.postalCode(), e.city(), e.country(),
                    // Absent on orders placed before the address was geocoded,
                    // and on any order whose lookup failed.
                    e.dropoffLatitude() == null || e.dropoffLongitude() == null
                            ? null
                            : new GeoLocation(e.dropoffLatitude(), e.dropoffLongitude()));
            this.orderLines = new ArrayList<>(e.lines().stream()
                    .map(li -> new OrderLine(li.dishId(), li.dishName(), li.unitPrice(), li.quantity()))
                    .toList());
            this.totalPrice = e.totalPrice();
            // An order starts unpaid, and no kitchen hears about it until the
            // provider says the money is held (US-23).
            this.status = OrderStatus.AWAITING_PAYMENT;
            this.payment = Payment.pending();
            this.placedAt = e.occurredOn();
            this.version = 0;
        } else if (event instanceof OrderPaymentAuthorizedEvent e) {
            this.payment = this.payment.authorized(e.paymentReference(), e.occurredAt());
            this.status = OrderStatus.PENDING_ACCEPTANCE;
            this.version++;
        } else if (event instanceof OrderPaymentCapturedEvent e) {
            this.payment = this.payment.captured(e.occurredAt());
            this.version++;
        } else if (event instanceof OrderPaymentReleasedEvent e) {
            this.payment = this.payment.released(e.occurredAt());
            this.version++;
        } else if (event instanceof OrderPaymentExpiredEvent) {
            this.status = OrderStatus.PAYMENT_EXPIRED;
            this.version++;
        } else if (event instanceof OrderAcceptedEvent e) {
            this.status = OrderStatus.ACCEPTED;
            this.acceptedAt = e.occurredAt();
            this.version++;
        } else if (event instanceof OrderRejectedEvent e) {
            this.status = OrderStatus.REJECTED;
            this.rejectionReason = e.rejectionReason();
            this.version++;
        } else if (event instanceof OrderReadyForPickupEvent e) {
            this.status = OrderStatus.READY_FOR_PICKUP;
            this.readyAt = e.occurredAt();
            this.version++;
        } else if (event instanceof OrderPickedUpEvent e) {
            this.status = OrderStatus.IN_DELIVERY;
            this.pickedUpAt = e.occurredAt();
            this.version++;
        } else if (event instanceof OrderDeliveredEvent e) {
            this.status = OrderStatus.DELIVERED;
            this.deliveredAt = e.occurredAt();
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
                payment,
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

    public Payment getPayment() {
        return payment;
    }

    /** What the provider calls this payment, {@code null} before checkout completes. */
    public String getPaymentReference() {
        return payment == null ? null : payment.reference();
    }

    public boolean hasHeldPayment() {
        return payment != null && payment.isHeld();
    }

    public boolean isAwaitingPayment() {
        return status == OrderStatus.AWAITING_PAYMENT;
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