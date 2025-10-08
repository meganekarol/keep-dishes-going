package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.CreateDishRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.EditDishRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateDishResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.CreateRestaurantResponse;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.EditDishResponse;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;
import be.kdg.keepdishesgoing.restaurant.port.in.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/dishes")
public class DishController {

    private final EditDishAsDraftUseCase editDishAsDraftUseCase;
    private final PublishAndUnpublishDishUseCase publishAndUnpublishDishUseCase;
    private final CreateDishUseCase createDishUseCase;
    private final MarkDishInAndOutOfStockUseCase markDishInAndOutOfStockUseCase;

    public DishController(EditDishAsDraftUseCase editDishAsDraftUseCase, PublishAndUnpublishDishUseCase publishAndUnpublishDishUseCase, CreateDishUseCase createDishUseCase, MarkDishInAndOutOfStockUseCase markDishInAndOutOfStockUseCase) {
        this.editDishAsDraftUseCase = editDishAsDraftUseCase;
        this.publishAndUnpublishDishUseCase = publishAndUnpublishDishUseCase;
        this.createDishUseCase = createDishUseCase;
        this.markDishInAndOutOfStockUseCase = markDishInAndOutOfStockUseCase;
    }

    @PostMapping("/new")
    public ResponseEntity<CreateDishResponse> addDish(@PathVariable UUID restaurantId,@RequestBody CreateDishRequest request) {
        // Map request DTO to command

        Set<FoodTag> foodTags = request.foodTags().stream()
                .map(FoodTag::valueOf)
                .collect(Collectors.toSet());

        CreateDishCommand command = new CreateDishCommand(
                restaurantId,
                request.name(),
                request.type(),
                foodTags,
                request.description(),
                request.price(),
                request.pictureUrl()
        );
        Dish dish = createDishUseCase.createDish(command); // Returns domain object

        // Map Domain to response DTO
        CreateDishResponse response = new CreateDishResponse(
                dish.getRestaurantId().uuid(),
                dish.getDishId().uuid(),
                dish.getDraftName(),
                dish.getDraftType(),
                dish.getDraftFoodTags(),
                dish.getDraftDescription(),
                dish.getDraftPrice().getAmount(),
                dish.getDraftPictureUrl().toString()
        );

        return new ResponseEntity<>(response,HttpStatus.CREATED);

    }

    @PutMapping("/{dishId}/draft")
    public ResponseEntity<EditDishResponse> editDish(
            @PathVariable UUID restaurantId,
            @PathVariable UUID dishId,
            @RequestBody EditDishRequest request) {

        // Map request DTO to Command
        EditDishAsDraftCommand command = new EditDishAsDraftCommand(
                restaurantId,
                dishId,
                request.name(),
                request.type(),
                request.foodTags(),
                request.description(),
                request.price(),
                request.pictureUrl()
        );

        Dish dishDraft = editDishAsDraftUseCase.editDish(command);

        // Map to Domain to DTO
        EditDishResponse response = new EditDishResponse(
                dishDraft.getDraftName(),
                dishDraft.getDraftType(),
                dishDraft.getDraftFoodTags(),
                dishDraft.getDraftDescription(),
                dishDraft.getDraftPrice().getAmount(),
                dishDraft.getDraftPictureUrl().toString()
        );

        return new ResponseEntity<>(response, HttpStatus.OK);

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
}
