package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateRestaurantRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.UpdateRestaurantHours;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.UpdateRestaurantStatusRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateRestaurantResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.mappers.RestaurantDtoMapper;
import be.kdg.keepdishesgoing.restaurant.core.GetOwnerRestaurantUseCaseImpl;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.port.in.ChangeRestaurantStatusUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.SetOpeningHoursUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ChangeRestaurantStatusCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.GetOwnerRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.SetOpeningHoursCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final SetOpeningHoursUseCase setOpeningHoursUseCase;
    private final ChangeRestaurantStatusUseCase changeRestaurantStatusUseCase;
    private final GetOwnerRestaurantUseCaseImpl getOwnerRestaurantUseCase;
    private final RestaurantDtoMapper mapper;

    public RestaurantController(CreateRestaurantUseCase createRestaurantUseCase, SetOpeningHoursUseCase setOpeningHoursUseCase, ChangeRestaurantStatusUseCase changeRestaurantStatusUseCase, GetOwnerRestaurantUseCaseImpl getOwnerRestaurantUseCase, RestaurantDtoMapper mapper) {
        this.createRestaurantUseCase = createRestaurantUseCase;
        this.setOpeningHoursUseCase = setOpeningHoursUseCase;
        this.changeRestaurantStatusUseCase = changeRestaurantStatusUseCase;
        this.getOwnerRestaurantUseCase = getOwnerRestaurantUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<CreateRestaurantResponse> createRestaurant(
            @RequestBody CreateRestaurantRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(jwt.getSubject());
        CreateRestaurantCommand command = mapper.toCommand(request, ownerId);
        Restaurant restaurant = createRestaurantUseCase.createRestaurant(command);
        CreateRestaurantResponse response = mapper.toResponse(restaurant);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyRestaurant(@AuthenticationPrincipal Jwt jwt) {
        UUID ownerId = UUID.fromString(jwt.getSubject());

        GetOwnerRestaurantCommand cmd = new GetOwnerRestaurantCommand(ownerId);

        return getOwnerRestaurantUseCase.getOwnerRestaurant(cmd)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{restaurantId}/opening-hours")
    public ResponseEntity<Void> setOpeningHours(@PathVariable UUID restaurantId,
                                                @RequestBody UpdateRestaurantHours request) {

        setOpeningHoursUseCase.setOpeningHours(new SetOpeningHoursCommand(restaurantId, request.schedule()));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{restaurantId}/status")
    public ResponseEntity<Void> changeStatus(@PathVariable UUID restaurantId,
                                             @RequestBody UpdateRestaurantStatusRequest request) {
        var status = request.status();
        changeRestaurantStatusUseCase.changeRestaurantStatus(new ChangeRestaurantStatusCommand(restaurantId, status));
        return ResponseEntity.ok().build();
    }

}
