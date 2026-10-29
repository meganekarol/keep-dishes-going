package be.kdg.keepdishesgoing.pricing.adapter.in.dto;

/**
 * What loading the demo history actually wrote.
 *
 * @param criteriaVersionsAdded 0 when the admin has already set thresholds
 * @param restaurantsSeeded     0 when every restaurant already has back-dated history
 */
public record DemoHistoryResponse(int criteriaVersionsAdded, int restaurantsSeeded) {
}
