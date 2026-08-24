package be.kdg.keepdishesgoing.pricing.domain.valueObjects;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * What a restaurant's menu averaged at one moment (US-26).
 */
public record MenuPricePoint(LocalDateTime at, BigDecimal averageMenuPrice) {
}
