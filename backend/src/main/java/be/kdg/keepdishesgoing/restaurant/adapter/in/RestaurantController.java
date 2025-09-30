package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateRestaurantRequest;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.PictureUrl;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final CreateRestaurantUseCase createRestaurantUseCase;

    public RestaurantController(CreateRestaurantUseCase createRestaurantUseCase) {
        this.createRestaurantUseCase = createRestaurantUseCase;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateRestaurantCommand> createRestaurant(@RequestBody CreateRestaurantRequest request) {

        List<PictureUrl> pictureUrls = request.pictureUrls().stream()
                .map(PictureUrl::new)
                .toList();

        UUID ownerId = UUID.randomUUID(); // Get later from authentication

        CreateRestaurantCommand command = new CreateRestaurantCommand(
                ownerId,
                request.name(),
                request.address(),
                request.contactEmail(),
                CuisineType.valueOf(request.cuisineType()),
                request.defaultPreparationTime(),
                request.openingHours(),
                pictureUrls
        );
        createRestaurantUseCase.createRestaurant(command);
        return new ResponseEntity<>(command, HttpStatus.CREATED);
    }
}
