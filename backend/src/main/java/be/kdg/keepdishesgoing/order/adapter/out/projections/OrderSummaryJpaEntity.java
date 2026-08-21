package be.kdg.keepdishesgoing.order.adapter.out.projections;

import be.kdg.keepdishesgoing.order.domain.valueObjects.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read model of the order module's own orders, listed per restaurant.
 *
 * <p>Orders are event sourced, so answering "what is waiting in my kitchen"
 * from the store itself would mean replaying every order ever placed. This
 * table is kept up to date by {@code OrderSummaryProjectionListener} and is
 * read by the owner's screen. It never drives a decision: accepting or
 * rejecting still loads the aggregate.
 */
@Entity
@Table(name = "order_summaries", schema = "kdg_order", indexes = {
    @Index(name = "idx_summary_restaurant", columnList = "restaurantId"),
    // The auto-rejection run asks for these two every minute.
    @Index(name = "idx_summary_status_pending", columnList = "status, pendingSince"),
    // As does the sweep for checkouts nobody paid for.
    @Index(name = "idx_summary_status_placed", columnList = "status, placedAt")
})
public class OrderSummaryJpaEntity {

    @Id
    private UUID orderId;

    @Column(nullable = false)
    private UUID restaurantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String deliveryAddress;

    /** The ordered dishes as one line of text, enough for the kitchen to read. */
    @Column(nullable = false, length = 2000)
    private String itemSummary;

    @Column(nullable = false)
    private int itemCount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDateTime placedAt;

    /**
     * When the order landed in front of its restaurant, which is when it was
     * paid for and not when the customer started checking out (US-23).
     *
     * <p>What the five-minute acceptance deadline counts from. Counting from
     * {@code placedAt} instead would spend the kitchen's window on the time the
     * customer took to enter a card.
     */
    private LocalDateTime pendingSince;

    private LocalDateTime acceptedAt;

    private LocalDateTime readyAt;

    @Column(length = 500)
    private String rejectionReason;

    protected OrderSummaryJpaEntity() {
        // For JPA
    }

    public OrderSummaryJpaEntity(UUID orderId, UUID restaurantId, String customerName,
                                 String deliveryAddress, String itemSummary, int itemCount,
                                 BigDecimal totalPrice, LocalDateTime placedAt) {
        this.orderId = orderId;
        this.restaurantId = restaurantId;
        // Not yet the kitchen's problem: an order is unpaid until the provider
        // says otherwise, and only then does it start counting down (US-23).
        this.status = OrderStatus.AWAITING_PAYMENT;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.itemSummary = itemSummary;
        this.itemCount = itemCount;
        this.totalPrice = totalPrice;
        this.placedAt = placedAt;
    }

    /**
     * Sets the lifecycle fields outright, for rebuilding a row from an order
     * that has already run its course.
     */
    public void restoreProgress(OrderStatus status, LocalDateTime pendingSince,
                                LocalDateTime acceptedAt, LocalDateTime readyAt,
                                String rejectionReason) {
        this.status = status;
        this.pendingSince = pendingSince;
        this.acceptedAt = acceptedAt;
        this.readyAt = readyAt;
        this.rejectionReason = rejectionReason;
    }

    /** The money is held, so the kitchen may now see the order (US-23). */
    public void markPendingAcceptance(LocalDateTime pendingSince) {
        this.status = OrderStatus.PENDING_ACCEPTANCE;
        this.pendingSince = pendingSince;
    }

    public void markPaymentExpired() {
        this.status = OrderStatus.PAYMENT_EXPIRED;
    }

    public void markAccepted(LocalDateTime acceptedAt) {
        this.status = OrderStatus.ACCEPTED;
        this.acceptedAt = acceptedAt;
    }

    public void markRejected(String reason) {
        this.status = OrderStatus.REJECTED;
        this.rejectionReason = reason;
    }

    public void markReady(LocalDateTime readyAt) {
        this.status = OrderStatus.READY_FOR_PICKUP;
        this.readyAt = readyAt;
    }

    public void markPickedUp() {
        this.status = OrderStatus.IN_DELIVERY;
    }

    public void markDelivered() {
        this.status = OrderStatus.DELIVERED;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getItemSummary() {
        return itemSummary;
    }

    public int getItemCount() {
        return itemCount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public LocalDateTime getPendingSince() {
        return pendingSince;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime getReadyAt() {
        return readyAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
