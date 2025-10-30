package be.kdg.keepdishesgoing.order.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for storing order snapshots for event sourcing optimization
 */
@Entity
@Table(name = "order_snapshots",schema = "kdg_order")
public class OrderSnapshotJpaEntity {
    
    @Id
    private UUID orderId;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String snapshotData;
    
    @Column(nullable = false)
    private Integer version;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    protected OrderSnapshotJpaEntity() {
        // For JPA
    }
    
    public OrderSnapshotJpaEntity(UUID orderId, String snapshotData, Integer version) {
        this.orderId = orderId;
        this.snapshotData = snapshotData;
        this.version = version;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters
    public UUID getOrderId() {
        return orderId;
    }
    
    public String getSnapshotData() {
        return snapshotData;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    // Setters for updates
    public void setSnapshotData(String snapshotData) {
        this.snapshotData = snapshotData;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}