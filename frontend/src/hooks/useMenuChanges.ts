import { useMutation, useQueryClient } from '@tanstack/react-query';

import { dishKeys } from './useOwnerMenu';
import { restaurantKeys } from './useRestaurants';
import { dishService } from '../services/dishService';

/**
 * Publish every pending draft at once (US-06).
 *
 * This is the batch counterpart of publishing one dish, so it invalidates the
 * public catalogue too. It can still fail as a whole: the cap on published
 * dishes is checked per dish as the batch runs.
 */
export function useApplyPendingChanges(restaurantId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => dishService.applyChanges(restaurantId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dishKeys.ownerMenu(restaurantId) });
      queryClient.invalidateQueries({ queryKey: restaurantKeys.all });
    },
  });
}

/**
 * Publish every pending draft at a chosen moment instead (US-07).
 *
 * Nothing reaches customers yet, so only the owner's view is invalidated. The
 * backend applies the batch itself once the time passes.
 */
export function useScheduleChanges(restaurantId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (scheduledTime: string) => dishService.scheduleChanges(restaurantId, scheduledTime),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dishKeys.ownerMenu(restaurantId) });
    },
  });
}