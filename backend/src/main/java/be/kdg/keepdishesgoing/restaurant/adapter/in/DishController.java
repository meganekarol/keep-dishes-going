package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.request.EditDishRequest;
import be.kdg.keepdishesgoing.restaurant.adapter.in.dto.response.EditDishResponse;
import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.port.in.EditDishAsDraftCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.EditDishAsDraftUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/dishes")
public class DishController {

    private final EditDishAsDraftUseCase editDishAsDraftUseCase;

    public DishController(EditDishAsDraftUseCase editDishAsDraftUseCase) {
        this.editDishAsDraftUseCase = editDishAsDraftUseCase;
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
}
