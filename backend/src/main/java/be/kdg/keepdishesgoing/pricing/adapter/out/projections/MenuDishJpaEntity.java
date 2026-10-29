package be.kdg.keepdishesgoing.pricing.adapter.out.projections;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * What one dish on a menu costs, as the Pricing context sees it.
 *
 * <p>The Pricing context cannot read the Restaurant context's tables, so it
 * keeps its own copy of the prices it needs, built from the dish events. Only
 * dishes currently on a menu are here: an unpublished dish is deleted.
 */
@Entity
@Table(name = "menu_dishes", schema = "kdg_pricing", indexes = {
        @Index(name = "idx_menu_dish_restaurant", columnList = "restaurantId")
})
public class MenuDishJpaEntity {

    @Id
    private UUID dishId;

    @Column(nullable = false)
    private UUID restaurantId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected MenuDishJpaEntity() {
        // For JPA
    }

    public MenuDishJpaEntity(UUID dishId, UUID restaurantId, BigDecimal price) {
        this.dishId = dishId;
        this.restaurantId = restaurantId;
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
