package be.kdg.keepdishesgoing.restaurant.port.in;

public interface PublishAndUnpublishDishUseCase {
    void publishDish(PublishDishCommand command);
    void unpublishDish(UnpublishDishCommand command);
}
