package be.kdg.keepdishesgoing.pricing.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for storing price range criteria events in the event store.
 *
 * <p>No aggregate id column: one policy governs the whole platform, so the
 * sequence number alone orders the log.
 */
@Entity
@Table(name = "price_range_criteria_events", schema = "kdg_pricing", indexes = {
        @Index(name = "idx_criteria_sequence", columnList = "sequenceNumber")
})
public class PriceRangeCriteriaEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventData;

    @Column(nullable = false)
    private LocalDateTime occurredOn;

    @Column(nullable = false)
    private Integer sequenceNumber;

    protected PriceRangeCriteriaEventJpaEntity() {
        // For JPA
    }

    public PriceRangeCriteriaEventJpaEntity(String eventType, String eventData,
                                            LocalDateTime occurredOn, Integer sequenceNumber) {
        this.eventType = eventType;
        this.eventData = eventData;
        this.occurredOn = occurredOn;
        this.sequenceNumber = sequenceNumber;
    }

    public UUID getId() {
        return id;
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
}
