import { useMutation, useQueryClient } from '@tanstack/react-query';

import { restaurantKeys } from './useRestaurants';
import { ownerService } from '../services/ownerService';
import type { ManualRestaurantStatus } from '../services/ownerService';

interface ChangeStatusVariables {
  restaurantId: string;
  status: ManualRestaurantStatus;
}

/**
 * Opens or closes the restaurant by hand (US-09).
 *
 * Invalidates the whole `restaurants` prefix rather than just the owner's own
 * lookup, because the brief wants customers to see the change immediately and
 * the catalogue caches an open flag of its own.
 */
export function useChangeRestaurantStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ restaurantId, status }: ChangeStatusVariables) =>
      ownerService.changeStatus(restaurantId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: restaurantKeys.all });
    },
  });
}

/** Hands the door back to the opening hours (US-09). */
export function useFollowSchedule() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (restaurantId: string) => ownerService.followSchedule(restaurantId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: restaurantKeys.all });
    },
  });
}