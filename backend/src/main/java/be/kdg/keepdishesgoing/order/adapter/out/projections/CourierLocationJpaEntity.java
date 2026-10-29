package be.kdg.keepdishesgoing.order.adapter.out.projections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The last place a courier reported being, per order (US-29).
 *
 * <p>One row per order, overwritten on every report. A courier sends a position
 * every few seconds; keeping the history would grow without bound to answer a
 * question nobody asks, and the tracking page only ever wants the latest.
 *
 * <p>Deliberately not part of the {@code Order} aggregate. The order is event
 * sourced, so recording each ping there would append thousands of events and
 * bury the handful that describe what actually happened to it.
 */
@Entity
@Table(name = "courier_locations", schema = "kdg_order")
public class CourierLocationJpaEntity {

    @Id
    private UUID orderId;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    @Column(nullable = false)
    private LocalDateTime reportedAt;

    protected CourierLocationJpaEntity() {
        // For JPA
    }

    public CourierLocationJpaEntity(UUID orderId, double lat, double lng, LocalDateTime reportedAt) {
        this.orderId = orderId;
        this.lat = lat;
        this.lng = lng;
        this.reportedAt = reportedAt;
    }

    /**
     * Ignores a report older than the one already stored. Messages can arrive
     * out of order after a redelivery, and a stale position would drag the
     * courier backwards on the customer's screen.
     */
    public void reportedAt(double newLat, double newLng, LocalDateTime when) {
        if (when != null && reportedAt != null && when.isBefore(reportedAt)) {
            return;
        }
        this.lat = newLat;
        this.lng = newLng;
        this.reportedAt = when;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }
}
