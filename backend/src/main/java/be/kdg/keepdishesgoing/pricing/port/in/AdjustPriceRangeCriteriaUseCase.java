package be.kdg.keepdishesgoing.pricing.port.in;

import be.kdg.keepdishesgoing.pricing.port.in.commands.AdjustPriceRangeCriteriaCommand;

/**
 * The platform admin setting what the price ranges mean (US-32).
 */
public interface AdjustPriceRangeCriteriaUseCase {

    /**
     * @throws IllegalArgumentException if the thresholds do not rise
     */
    void adjust(AdjustPriceRangeCriteriaCommand command);
}
