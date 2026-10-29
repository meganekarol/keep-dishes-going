package be.kdg.keepdishesgoing.pricing.adapter.in;

import be.kdg.keepdishesgoing.pricing.adapter.in.dto.AdjustPriceRangeCriteriaRequest;
import be.kdg.keepdishesgoing.pricing.domain.valueObjects.CriteriaVersion;
import be.kdg.keepdishesgoing.pricing.port.in.AdjustPriceRangeCriteriaUseCase;
import be.kdg.keepdishesgoing.pricing.port.in.ViewPriceRangeCriteriaQuery;
import be.kdg.keepdishesgoing.pricing.port.in.commands.AdjustPriceRangeCriteriaCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The platform admin's price range criteria (US-32).
 *
 * <p>Only KDG itself decides what "expensive" means, so this is the one part of
 * the API that belongs to neither an owner nor a customer.
 */
@RestController
@RequestMapping("/api/price-range-criteria")
public class PriceRangeCriteriaController {

    private final AdjustPriceRangeCriteriaUseCase adjustPriceRangeCriteriaUseCase;
    private final ViewPriceRangeCriteriaQuery viewPriceRangeCriteriaQuery;

    public PriceRangeCriteriaController(AdjustPriceRangeCriteriaUseCase adjustPriceRangeCriteriaUseCase,
                                        ViewPriceRangeCriteriaQuery viewPriceRangeCriteriaQuery) {
        this.adjustPriceRangeCriteriaUseCase = adjustPriceRangeCriteriaUseCase;
        this.viewPriceRangeCriteriaQuery = viewPriceRangeCriteriaQuery;
    }

    /** Every version set so far, in the order they take effect. */
    @GetMapping
    public ResponseEntity<List<CriteriaVersion>> timeline() {
        return ResponseEntity.ok(viewPriceRangeCriteriaQuery.timeline());
    }

    /**
     * Sets new thresholds, from a date that may be in the past.
     *
     * <p>Never a PUT: this appends a version to a history rather than replacing
     * the thresholds, and the older versions keep governing their own periods.
     */
    @PostMapping
    public ResponseEntity<Void> adjust(@RequestBody AdjustPriceRangeCriteriaRequest request) {
        adjustPriceRangeCriteriaUseCase.adjust(new AdjustPriceRangeCriteriaCommand(
                request.effectiveFrom().atStartOfDay(),
                request.cheapUnder(),
                request.regularUnder(),
                request.expensiveUnder()));

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
