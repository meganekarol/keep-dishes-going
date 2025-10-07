package be.kdg.keepdishesgoing.restaurant.port.in;

import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.*;

import java.util.List;
import java.util.UUID;

public record CreateRestaurantCommand(
        UUID ownerId,
        String name,
        Address address,
        String contactEmail,
        CuisineType cuisineType,
        int defaultPreparationTime,
        OpeningHours openingHours,
        List<PictureUrl> pictureUrls
) {

}
