package be.kdg.keepdishesgoing.pricing.adapter.out.persistence;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One recorded average for a restaurant's menu (US-26).
 *
 * <p>Append only. The restaurant context keeps only the version of a dish that
 * is live now, so without this log there is no way back to what a menu cost
 * last month.
 */
@Entity
@Table(name = "menu_price_points", schema = "kdg_pricing", indexes = {
        @Index(name = "idx_menu_price_restaurant", columnList = "restaurantId,recordedAt")
})
public class MenuPricePointJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private UUID restaurantId;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal averageMenuPrice;

    protected MenuPricePointJpaEntity() {
        // For JPA
    }

    public MenuPricePointJpaEntity(UUID restaurantId, LocalDateTime recordedAt,
                                   BigDecimal averageMenuPrice) {
        this.restaurantId = restaurantId;
        this.recordedAt = recordedAt;
        this.averageMenuPrice = averageMenuPrice;
    }

    public UUID getRestaurantId() {
        return restaurantId;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public BigDecimal getAverageMenuPrice() {
        return averageMenuPrice;
    }
}
