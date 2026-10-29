package be.kdg.keepdishesgoing.order.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for storing order domain events in the event store
 */
@Entity
@Table(name = "order_events", schema = "kdg_order", indexes = {
    @Index(name = "idx_order_id", columnList = "orderId"),
    @Index(name = "idx_event_type", columnList = "eventType"),
    @Index(name = "idx_occurred_on", columnList = "occurredOn")
})
public class OrderEventJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(nullable = false)
    private UUID orderId;
    
    @Column(nullable = false)
    private String eventType;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventData;
    
    @Column(nullable = false)
    private LocalDateTime occurredOn;
    
    @Column(nullable = false)
    private Integer sequenceNumber;
    
    @Version
    private Long version;
    
    protected OrderEventJpaEntity() {
        // For JPA
    }
    
    public OrderEventJpaEntity(UUID orderId, String eventType, String eventData, 
                               LocalDateTime occurredOn, Integer sequenceNumber) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.eventData = eventData;
        this.occurredOn = occurredOn;
        this.sequenceNumber = sequenceNumber;
    }
    
    // Getters
    public UUID getId() {
        return id;
    }
    
    public UUID getOrderId() {
        return orderId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public String getEventData() {
        return eventData;
    }
    
    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
    
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }
    
    public Long getVersion() {
        return version;
    }
}