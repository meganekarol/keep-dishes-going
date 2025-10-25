package be.kdg.keepdishesgoing.restaurant.adapter.in.mappers;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateRestaurantRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateRestaurantResponse;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OpeningHours;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateRestaurantCommand;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantDtoMapper {
    // DTO → Command
    public CreateRestaurantCommand toCommand(CreateRestaurantRequest request, UUID ownerId) {
        OpeningHours openingHours = new OpeningHours(
                request.openingHours().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new TimeRange(e.getValue().open(), e.getValue().closed())
                        ))
        );
        return new CreateRestaurantCommand(
                ownerId,
                request.name(),
                request.address(),
                request.contactEmail(),
                CuisineType.valueOf(request.cuisineType()),
                request.defaultPreparationTime(),
                openingHours,
                request.pictureUrls()
        );
    }

    // Domain → DTO
    public CreateRestaurantResponse toResponse(Restaurant restaurant) {
        Map<DayOfWeek, TimeRange> openingHoursResponse = restaurant.getOpeningHours()
                .schedule()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new TimeRange(
                                e.getValue().open(),
                                e.getValue().closed()
                        )
                ));

        return new CreateRestaurantResponse(
                restaurant.getRestaurantId().uuid(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getContactEmail(),
                restaurant.getCuisineType().toString(),
                restaurant.getDefaultPrepTime(),
                openingHoursResponse,
                restaurant.getPictureUrls(),
                restaurant.getStatus().toString()
        );
    }
}

