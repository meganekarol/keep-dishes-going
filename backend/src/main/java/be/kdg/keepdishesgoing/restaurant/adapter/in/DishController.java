package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateDishRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.EditDishRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateDishResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.EditDishResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.mappers.DishDtoMapper;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;
import be.kdg.keepdishesgoing.restaurant.port.in.*;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Every endpoint here targets a specific restaurant, so ownership is asserted
 * once at class level. The expression is evaluated per invocation, which is why
 * {@code #restaurantId} resolves against each method's own arguments.
 */
@RestController
@RequestMapping("/api/{restaurantId}/dishes")
@PreAuthorize("@restaurantOwnership.isOwner(#restaurantId, authentication.name)")
public class DishController {

    private final EditDishAsDraftUseCase editDishAsDraftUseCase;
    private final PublishAndUnpublishDishUseCase publishAndUnpublishDishUseCase;
    private final CreateDishUseCase createDishUseCase;
    private final MarkDishInAndOutOfStockUseCase markDishInAndOutOfStockUseCase;
    private final ApplyPendingChangesUseCase applyPendingChangesUseCase;
    private final ScheduleChangesUseCase scheduleChangesUseCase;
    private final DishDtoMapper mapper;

    public DishController(EditDishAsDraftUseCase editDishAsDraftUseCase, PublishAndUnpublishDishUseCase publishAndUnpublishDishUseCase, CreateDishUseCase createDishUseCase, MarkDishInAndOutOfStockUseCase markDishInAndOutOfStockUseCase, ApplyPendingChangesUseCase applyPendingChangesUseCase, ScheduleChangesUseCase scheduleChangesUseCase, DishDtoMapper mapper) {
        this.editDishAsDraftUseCase = editDishAsDraftUseCase;
        this.publishAndUnpublishDishUseCase = publishAndUnpublishDishUseCase;
        this.createDishUseCase = createDishUseCase;
        this.markDishInAndOutOfStockUseCase = markDishInAndOutOfStockUseCase;
        this.applyPendingChangesUseCase = applyPendingChangesUseCase;
        this.scheduleChangesUseCase = scheduleChangesUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/new")
    public ResponseEntity<CreateDishResponse> addDish(@PathVariable UUID restaurantId,@RequestBody CreateDishRequest request) {

        var command = mapper.toCreateCommand(restaurantId, request);
        var dish = createDishUseCase.createDish(command);
        var response = mapper.toCreateResponse(dish);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PutMapping("/{dishId}/draft")
    public ResponseEntity<EditDishResponse> editDish(
            @PathVariable UUID restaurantId,
            @PathVariable UUID dishId,
            @RequestBody EditDishRequest request) {

        var command = mapper.toEditCommand(restaurantId, dishId, request);
        var dishDraft = editDishAsDraftUseCase.editDish(command);
        var response = mapper.toEditResponse(dishDraft);

        return ResponseEntity.ok(response);

    }

    @PostMapping("/{dishId}/publish")
    public ResponseEntity<Void> publishDish(@PathVariable UUID restaurantId, @PathVariable UUID dishId) {

        PublishDishCommand command = new PublishDishCommand(restaurantId, dishId);
        publishAndUnpublishDishUseCase.publishDish(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{dishId}/unpublish")
    public ResponseEntity<Void> unpublishDish(@PathVariable UUID restaurantId, @PathVariable UUID dishId) {

        UnpublishDishCommand command = new UnpublishDishCommand(restaurantId, dishId);
        publishAndUnpublishDishUseCase.unpublishDish(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{dishId}/mark-in-stock")
    public ResponseEntity<Void> markDishInStock(@PathVariable UUID restaurantId, @PathVariable UUID dishId) {

        MarkDishInStockCommand command = new MarkDishInStockCommand(restaurantId, dishId);
        markDishInAndOutOfStockUseCase.markDishInStock(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{dishId}/mark-out-of-stock")
    public ResponseEntity<Void> markDishOutOfStock(@PathVariable UUID restaurantId, @PathVariable UUID dishId) {

        MarkDishOutOfStockCommand command = new MarkDishOutOfStockCommand(restaurantId, dishId);
        markDishInAndOutOfStockUseCase.markDishOutOfStock(command);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/schedule-changes")
    public ResponseEntity<Void> scheduleChanges(
            @PathVariable UUID restaurantId,
            @RequestParam LocalDateTime scheduledTime) {

        ScheduleDishChangesCommand command = new ScheduleDishChangesCommand(restaurantId, scheduledTime);

        scheduleChangesUseCase.schedulePendingChanges(command);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/apply-changes")
    public ResponseEntity<Void> applyPendingChanges(@PathVariable UUID restaurantId) {

        ApplyPendingChangesCommand command = new ApplyPendingChangesCommand(restaurantId);
        applyPendingChangesUseCase.applyPendingChanges(command);
        return ResponseEntity.ok().build();
    }
}
