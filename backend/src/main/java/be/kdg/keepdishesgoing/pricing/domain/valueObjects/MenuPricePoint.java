package be.kdg.keepdishesgoing.pricing.domain.valueObjects;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * What a restaurant's menu averaged at one moment (US-26).
 *
 * @param at               when the average was recorded
 * @param averageMenuPrice the average price of the published dishes at that moment
 */
public record MenuPricePoint(LocalDateTime at, BigDecimal averageMenuPrice) {
}
