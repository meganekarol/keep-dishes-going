import { useContext } from 'react';
import { useQuery } from '@tanstack/react-query';

import BasketContext, { type BasketLine } from '../context/BasketContext';
import { restaurantService } from '../services/restaurantService';
import { restaurantKeys } from './useRestaurants';
import type { Availability } from '../types/api';

export type UnavailableReason = Exclude<Availability, 'AVAILABLE'>;

export interface UnavailableLine extends BasketLine {
  reason: UnavailableReason;
}

/** What to call each reason on screen. */
export const UNAVAILABLE_LABEL: Record<UnavailableReason, string> = {
  OUT_OF_STOCK: 'Sold out',
  REMOVED: 'No longer on the menu',
};

/**
 * The basket lines that can no longer be ordered (US-21).
 */
export function useBasketAvailability() {
  const { basket } = useContext(BasketContext);
  const lines = basket?.lines ?? [];
  const restaurantId = basket?.restaurantId ?? '';
  const dishIds = lines.map((line) => line.dishId);

  const { data, isLoading } = useQuery({
    queryKey: restaurantKeys.availability(restaurantId, dishIds),
    queryFn: () => restaurantService.checkAvailability(restaurantId, dishIds),
    enabled: restaurantId !== '' && dishIds.length > 0,
    // Overrides the app-wide 30 second staleTime and the disabled focus
    // refetching. The whole story is about a change made while the customer was
    // browsing, so a cached answer is worse than no answer.
    staleTime: 0,
    refetchOnMount: 'always',
    refetchOnWindowFocus: true,
  });

  const unavailable: UnavailableLine[] = lines.flatMap((line) => {
    const answer = data?.find((dish) => dish.dishId === line.dishId);
    // No answer yet means not yet known, not unavailable. isChecking covers that.
    if (!answer || answer.availability === 'AVAILABLE') return [];
    return [{ ...line, reason: answer.availability }];
  });

  return { unavailable, isChecking: isLoading };
}