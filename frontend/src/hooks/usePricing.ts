import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import { pricingService } from '../services/pricingService';

export const pricingKeys = {
  all: ['pricing'] as const,
  criteria: ['pricing', 'criteria'] as const,
  evolution: (restaurantId: string) => ['pricing', 'evolution', restaurantId] as const,
};

/** A restaurant's price range over time (US-26). */
export function usePriceRangeEvolution(restaurantId: string) {
  return useQuery({
    queryKey: pricingKeys.evolution(restaurantId),
    queryFn: () => pricingService.evolutionOf(restaurantId),
    enabled: Boolean(restaurantId),
  });
}

/** Every set of thresholds the platform has used (US-32). */
export function usePriceRangeCriteria() {
  return useQuery({
    queryKey: pricingKeys.criteria,
    queryFn: pricingService.criteriaTimeline,
  });
}

/**
 * Sets new thresholds (US-32).
 */
export function useAdjustPriceRangeCriteria() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: pricingService.adjustCriteria,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: pricingKeys.all }),
  });
}