package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateRestaurantRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.UpdateRestaurantStatusRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateRestaurantResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.mappers.RestaurantDtoMapper;
import be.kdg.keepdishesgoing.restaurant.core.GetOwnerRestaurantUseCaseImpl;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OwnerId;
import be.kdg.keepdishesgoing.restaurant.port.in.ChangeRestaurantStatusUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.ChangeRestaurantStatusCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.GetOwnerRestaurantCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final ChangeRestaurantStatusUseCase changeRestaurantStatusUseCase;
    private final GetOwnerRestaurantUseCaseImpl getOwnerRestaurantUseCase;
    private final RestaurantDtoMapper mapper;

    public RestaurantController(CreateRestaurantUseCase createRestaurantUseCase, ChangeRestaurantStatusUseCase changeRestaurantStatusUseCase, GetOwnerRestaurantUseCaseImpl getOwnerRestaurantUseCase, RestaurantDtoMapper mapper) {
        this.createRestaurantUseCase = createRestaurantUseCase;
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

    /**
     * Open or close the doors by hand (US-09). Only the two manual states are
     * accepted; OPEN and CLOSED are worked out from the schedule, not set.
     */
    @PatchMapping("/{restaurantId}/status")
    @PreAuthorize("@restaurantOwnership.isOwner(#restaurantId, authentication.name)")
    public ResponseEntity<Void> changeStatus(@PathVariable UUID restaurantId,
                                             @RequestBody UpdateRestaurantStatusRequest request) {
        var status = request.status();
        changeRestaurantStatusUseCase.changeRestaurantStatus(new ChangeRestaurantStatusCommand(restaurantId, status));
        return ResponseEntity.ok().build();
    }

    /**
     * Remove the manual override, so the weekly schedule decides again (US-09).
     *
     * <p>Modelled as deleting the override rather than as another status value:
     * an owner resuming their hours is not choosing to be open or closed, they
     * are giving up the choice.
     */
    @DeleteMapping("/{restaurantId}/status")
    @PreAuthorize("@restaurantOwnership.isOwner(#restaurantId, authentication.name)")
    public ResponseEntity<Void> followSchedule(@PathVariable UUID restaurantId) {
        changeRestaurantStatusUseCase.followSchedule(restaurantId);
        return ResponseEntity.noContent().build();
    }

}
