package be.kdg.keepdishesgoing.pricing.adapter.in;

import be.kdg.keepdishesgoing.pricing.port.in.ViewPriceRangeEvolutionQuery;
import be.kdg.keepdishesgoing.pricing.port.in.queries.PriceRangeEvolution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * How a restaurant's price range moved over time (US-26).
 *
 * <p>Hung off the restaurant's own URL because that is where a customer meets
 * it. The assignment calls the graph an extra service for customers, so it is
 * as open as the menu it describes.
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/price-range-evolution")
public class PriceRangeEvolutionController {

    private final ViewPriceRangeEvolutionQuery viewPriceRangeEvolutionQuery;

    public PriceRangeEvolutionController(ViewPriceRangeEvolutionQuery viewPriceRangeEvolutionQuery) {
        this.viewPriceRangeEvolutionQuery = viewPriceRangeEvolutionQuery;
    }

    @GetMapping
    public ResponseEntity<PriceRangeEvolution> evolution(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(viewPriceRangeEvolutionQuery.evolutionOf(restaurantId));
    }
}
