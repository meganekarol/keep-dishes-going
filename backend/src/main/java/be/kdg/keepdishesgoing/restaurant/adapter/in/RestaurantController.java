package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateRestaurantRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.EditDishRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateRestaurantResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.EditDishResponse;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OpeningHours;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.PictureUrl;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.EditDishAsDraftCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.EditDishAsDraftUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final CreateRestaurantUseCase createRestaurantUseCase;

    public RestaurantController(CreateRestaurantUseCase createRestaurantUseCase) {
        this.createRestaurantUseCase = createRestaurantUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateRestaurantResponse> createRestaurant(@RequestBody CreateRestaurantRequest request) {

        UUID ownerId = UUID.randomUUID(); // Get later from authentication

        // Map DTO to Command
        List<PictureUrl> pictureUrls = request.pictureUrls().stream()
                .map(PictureUrl::new)
                .toList();

        OpeningHours openingHours = new OpeningHours(
                request.openingHours().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, e -> new TimeRange(e.getValue().open(), e.getValue().closed())
                        ))
        );

        CreateRestaurantCommand command = new CreateRestaurantCommand(
                ownerId,
                request.name(),
                request.address(),
                request.contactEmail(),
                CuisineType.valueOf(request.cuisineType()),
                request.defaultPreparationTime(),
                openingHours,
                pictureUrls
        );
       Restaurant restaurant =  createRestaurantUseCase.createRestaurant(command); // Returns the domain Model

        // Map domain to dto

        Map<DayOfWeek, TimeRange> openingHoursResponse = restaurant.getOpeningHours().schedule().entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new TimeRange(e.getValue().open(), e.getValue().closed())));

        CreateRestaurantResponse response = new CreateRestaurantResponse(
                restaurant.getRestaurantId().uuid(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getContactEmail(),
                restaurant.getCuisineType().toString(),
                restaurant.getDefaultPrepTime(),
                openingHoursResponse,
                restaurant.getPictureUrls().stream().map(PictureUrl::value).toList()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
