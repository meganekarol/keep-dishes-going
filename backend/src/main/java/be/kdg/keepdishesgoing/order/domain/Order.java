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
 * A customer's order, from checkout to the door.
 *
 * <p>Stored as events rather than as a row. Business methods check the rules and
 * then record an event, which is what changes the state. Rebuilding an order
 * replays its events onto a {@link #blank()} aggregate, or onto the newest
 * {@link OrderSnapshot}.
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

    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();

    private Order() {
        this.orderLines = new ArrayList<>();
    }

    /**
     * Creates an empty aggregate to replay events onto.
     *
     * @return a new order with nothing applied yet
     */
    public static Order blank() {
        return new Order();
    }

    /**
     * Places a new order (US-22). It starts unpaid, so no restaurant hears about
     * it yet.
     *
     * @param orderId         the order's identifier
     * @param customerInfo    who is ordering and how to reach them
     * @param restaurantId    the restaurant the order is for
     * @param deliveryAddress where the food is going
     * @param orderLines      the dishes ordered, priced off the menu shown
     * @return the new order, with its placed event waiting to be saved
     * @throws IllegalArgumentException if there are no order lines
     */
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

    /**
     * Rebuilds an order from a saved snapshot, so only later events need replaying.
     *
     * @param snapshot the saved state to start from
     * @return the order as it was when the snapshot was taken
     */
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

    /**
     * Records that the provider is holding the money, which is what makes the
     * order visible to its restaurant (US-23).
     *
     * <p>A repeat of a hold already recorded is ignored, since callbacks arrive
     * more than once.
     *
     * @param paymentReference the provider's id for the payment being held
     * @return {@code true} if this call changed anything
     * @throws IllegalStateException if the order was not awaiting payment
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
     * Records that the money has been taken, as the restaurant accepts (US-23).
     *
     * @throws IllegalStateException if there is no held payment to take
     */
    public void recordPaymentCaptured() {
        if (!payment.isHeld()) {
            throw new IllegalStateException("There is no held payment to take");
        }
        applyAndRecord(new OrderPaymentCapturedEvent(
                orderId.uuid(), restaurantId.uuid(), payment.reference(), totalPrice));
    }

    /**
     * Gives the hold back, for an order no kitchen is going to cook (US-23).
     *
     * @throws IllegalStateException if there is no held payment to give back
     */
    public void recordPaymentReleased() {
        if (!payment.isHeld()) {
            throw new IllegalStateException("There is no held payment to give back");
        }
        applyAndRecord(new OrderPaymentReleasedEvent(
                orderId.uuid(), restaurantId.uuid(), payment.reference()));
    }

    /**
     * Abandons a checkout the customer walked away from (US-23).
     *
     * @throws IllegalStateException if the order was not awaiting payment
     */
    public void expirePayment() {
        if (status != OrderStatus.AWAITING_PAYMENT) {
            throw new IllegalStateException("Only an order awaiting payment can expire");
        }
        applyAndRecord(new OrderPaymentExpiredEvent(orderId.uuid(), restaurantId.uuid()));
    }

    /**
     * Checks the kitchen may take this order on. Asked before the money is taken,
     * which cannot be undone.
     *
     * @throws IllegalStateException if the order is not waiting for a decision,
     *                               or its money is not held
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
     * Takes the order on, which also sends it to the delivery service (US-28).
     *
     * <p>The pickup address is passed in because an order knows which restaurant
     * it belongs to but not where that restaurant is.
     *
     * @param pickupAddress     the restaurant's address
     * @param pickupCoordinates where the restaurant is, or {@code null} if it was
     *                          never geocoded
     * @throws IllegalStateException if the order is not waiting for a decision,
     *                               or its money has not been taken
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

    /**
     * Reads the delivery address as a map point for the delivery service.
     *
     * @return the dropoff point, or {@code null} if the address was never geocoded
     */
    private OrderAcceptedEvent.Point dropoffCoordinates() {
        GeoLocation located = deliveryAddress.location();
        return located == null ? null : new OrderAcceptedEvent.Point(located.lat(), located.lng());
    }

    /**
     * Turns the order away, with a reason for the customer (US-10).
     *
     * @param reason why the restaurant will not cook it
     * @throws IllegalStateException if the order is not waiting for a decision
     */
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

    /**
     * Turns the order away because the restaurant did not decide in time (US-11).
     *
     * @throws IllegalStateException if the order is not waiting for a decision
     */
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

    /**
     * Marks the food cooked and waiting for a courier (US-12).
     *
     * @throws IllegalStateException if the restaurant has not accepted the order
     */
    public void markReady() {
        if (status != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order must be accepted before marking ready");
        }
        applyAndRecord(new OrderReadyForPickupEvent(orderId.uuid(), restaurantId.uuid()));
    }

    /**
     * Records that a courier has collected the food (US-29).
     *
     * @throws IllegalStateException if the order was not waiting for pickup
     */
    public void markPickedUp() {
        if (status != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalStateException("Order must be ready before being picked up");
        }
        applyAndRecord(new OrderPickedUpEvent(orderId.uuid(), restaurantId.uuid()));
    }

    /**
     * Records that the food reached the customer (US-29).
     *
     * @throws IllegalStateException if the order was not out for delivery
     */
    public void markDelivered() {
        if (status != OrderStatus.IN_DELIVERY) {
            throw new IllegalStateException("Order must be in delivery before being delivered");
        }
        applyAndRecord(new OrderDeliveredEvent(orderId.uuid(), restaurantId.uuid()));
    }

    /**
     * Applies one event to the order without recording it again. Used when
     * replaying stored events.
     *
     * @param event the event to apply
     * @throws IllegalStateException if the event is not one this order knows
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

    /**
     * Takes a copy of the order's current state, to save as a snapshot.
     *
     * @return the order's state right now
     */
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

    /**
     * Checks an order has something on it.
     *
     * @param orderLines the lines to check
     * @throws IllegalArgumentException if the list is missing or empty
     */
    private static void validateOrderLines(List<OrderLine> orderLines) {
        if (orderLines == null || orderLines.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one order line");
        }
    }

    /**
     * Adds up what the order comes to.
     *
     * @param orderLines the lines to add up
     * @return the total price
     */
    private static BigDecimal calculateTotalPrice(List<OrderLine> orderLines) {
        return orderLines.stream()
                .map(OrderLine::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns the events raised since this order was loaded.
     *
     * @return a copy of the events that still need saving
     */
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }

    /** Empties the list of uncommitted events, after they have been saved. */
    public void clearUncommittedEvents() {
        uncommittedEvents.clear();
    }

    /** @return the order's identifier, which the customer tracks it with */
    public OrderId getOrderId() {
        return orderId;
    }

    /** @return who placed the order and how to reach them */
    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    /** @return the restaurant the order is for */
    public RestaurantId getRestaurantId() {
        return restaurantId;
    }

    /** @return where the food is going */
    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    /** @return a copy of the dishes on the order */
    public List<OrderLine> getOrderLines() {
        return new ArrayList<>(orderLines);
    }

    /** @return what the order comes to */
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    /** @return where the order is, from checkout to the door */
    public OrderStatus getStatus() {
        return status;
    }

    /** @return when the order was placed */
    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    /** @return when the restaurant accepted it, or {@code null} if it has not */
    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    /** @return when it was ready for pickup, or {@code null} if it is not yet */
    public LocalDateTime getReadyAt() {
        return readyAt;
    }

    /** @return when a courier collected it, or {@code null} if none has */
    public LocalDateTime getPickedUpAt() {
        return pickedUpAt;
    }

    /** @return when it arrived, or {@code null} if it has not */
    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    /** @return why the order was turned away, or {@code null} if it was not */
    public String getRejectionReason() {
        return rejectionReason;
    }

    /** @return how many events have been applied to this order */
    public int getVersion() {
        return version;
    }

    /** @return where the order's money is */
    public Payment getPayment() {
        return payment;
    }

    /**
     * Returns what the provider calls this payment.
     *
     * @return the provider's payment id, or {@code null} before checkout completes
     */
    public String getPaymentReference() {
        return payment == null ? null : payment.reference();
    }

    /**
     * Checks whether the money is being held.
     *
     * @return {@code true} if the provider is holding it
     */
    public boolean hasHeldPayment() {
        return payment != null && payment.isHeld();
    }

    /**
     * Checks whether the customer has still not paid.
     *
     * @return {@code true} if the order is awaiting payment
     */
    public boolean isAwaitingPayment() {
        return status == OrderStatus.AWAITING_PAYMENT;
    }

    /**
     * Checks whether the restaurant still has to decide.
     *
     * @return {@code true} if the order is pending acceptance
     */
    public boolean isPendingAcceptance() {
        return status == OrderStatus.PENDING_ACCEPTANCE;
    }

    /**
     * Checks whether the restaurant took the order on.
     *
     * @return {@code true} if the order was accepted
     */
    public boolean isAccepted() {
        return status == OrderStatus.ACCEPTED;
    }

    /**
     * Checks whether the restaurant turned the order away.
     *
     * @return {@code true} if the order was rejected
     */
    public boolean isRejected() {
        return status == OrderStatus.REJECTED;
    }

    /**
     * Checks whether the food is waiting for a courier.
     *
     * @return {@code true} if the order is ready for pickup
     */
    public boolean isReadyForPickup() {
        return status == OrderStatus.READY_FOR_PICKUP;
    }

    /**
     * Checks whether a courier is out with the food.
     *
     * @return {@code true} if the order is in delivery
     */
    public boolean isInDelivery() {
        return status == OrderStatus.IN_DELIVERY;
    }

    /**
     * Checks whether the food reached the customer.
     *
     * @return {@code true} if the order was delivered
     */
    public boolean isDelivered() {
        return status == OrderStatus.DELIVERED;
    }
}
