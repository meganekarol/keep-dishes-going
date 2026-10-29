package be.kdg.keepdishesgoing.restaurant.adapter.out.embeddable;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;

@Embeddable
public class DishVersionEmbeddable {
    private String name;
    @Enumerated(EnumType.STRING)
    private DishType type;
    private String foodTags;
    private String description;
    private BigDecimal price;
    private String pictureUrl;

    public DishVersionEmbeddable(String name, DishType type,String foodTags, String description, BigDecimal price, String pictureUrl) {
        this.name = name;
        this.type = type;
        this.foodTags = foodTags;
        this.description = description;
        this.price = price;
        this.pictureUrl = pictureUrl;
    }

    public DishVersionEmbeddable() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DishType getType() {
        return type;
    }

    public void setType(DishType type) {
        this.type = type;
    }

    public String getFoodTags() {
        return foodTags;
    }

    public void setFoodTags(String foodTags) {
        this.foodTags = foodTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
