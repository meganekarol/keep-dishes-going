package be.kdg.keepdishesgoing.pricing.adapter.in;

import be.kdg.keepdishesgoing.pricing.adapter.in.dto.DemoHistoryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Loads back-dated sample history for the evolution graph (US-26, US-32).
 *
 * <p>Admin only, like everything else that decides what the price ranges mean.
 */
@RestController
@RequestMapping("/api/pricing")
public class PricingDemoDataController {

    private final DemoPriceHistorySeeder seeder;

    public PricingDemoDataController(DemoPriceHistorySeeder seeder) {
        this.seeder = seeder;
    }

    @PostMapping("/demo-history")
    public ResponseEntity<DemoHistoryResponse> loadDemoHistory() {
        return ResponseEntity.ok(seeder.seed());
    }
}
