package be.kdg.keepdishesgoing.restaurant.domain.valueObjects;

public record PictureUrl(String value) {
    public PictureUrl {
        if (!value.startsWith("http")) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }
}
