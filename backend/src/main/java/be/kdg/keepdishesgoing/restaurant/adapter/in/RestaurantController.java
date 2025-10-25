package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateRestaurantRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateRestaurantResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.mappers.RestaurantDtoMapper;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.RestaurantStatus;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;
import be.kdg.keepdishesgoing.restaurant.port.in.ChangeRestaurantStatusUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.SetOpeningHoursUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ChangeRestaurantStatusCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.SetOpeningHoursCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final SetOpeningHoursUseCase setOpeningHoursUseCase;
    private final ChangeRestaurantStatusUseCase changeRestaurantStatusUseCase;
    private final RestaurantDtoMapper mapper;

    public RestaurantController(CreateRestaurantUseCase createRestaurantUseCase, SetOpeningHoursUseCase setOpeningHoursUseCase, ChangeRestaurantStatusUseCase changeRestaurantStatusUseCase, RestaurantDtoMapper mapper) {
        this.createRestaurantUseCase = createRestaurantUseCase;
        this.setOpeningHoursUseCase = setOpeningHoursUseCase;
        this.changeRestaurantStatusUseCase = changeRestaurantStatusUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/new")
    public ResponseEntity<CreateRestaurantResponse> createRestaurant(@RequestBody CreateRestaurantRequest request) {

        UUID ownerId = UUID.randomUUID(); // Get later from authentication

        CreateRestaurantCommand command = mapper.toCommand(request, ownerId);

        //Execute use case (returns Domain Model)
        Restaurant restaurant = createRestaurantUseCase.createRestaurant(command);

        // Map domain to dto
        CreateRestaurantResponse response = mapper.toResponse(restaurant);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PutMapping("/{restaurantId}/opening-hours")
    public ResponseEntity<Void> setOpeningHours(@PathVariable UUID restaurantId,
                                                @RequestBody Map<DayOfWeek, TimeRange> schedule) {
        setOpeningHoursUseCase.setOpeningHours(new SetOpeningHoursCommand(restaurantId, schedule));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{restaurantId}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable UUID restaurantId,
                                             @RequestParam RestaurantStatus status) {
        changeRestaurantStatusUseCase.changeRestaurantStatus(new ChangeRestaurantStatusCommand(restaurantId, status));
        return ResponseEntity.ok().build();
    }
}
