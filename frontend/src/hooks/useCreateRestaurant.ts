import { useMutation, useQueryClient } from '@tanstack/react-query';

import { ownerKeys } from './useMyRestaurant';
import { ownerService } from '../services/ownerService';

/**
 * Creates the owner's restaurant (US-02).
 *
 * The cached "do I have a restaurant" answer is a 404-shaped null until this
 * succeeds, so it is invalidated here. Without that the dashboard would keep
 * bouncing back to this form.
 */
export function useCreateRestaurant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ownerService.createRestaurant,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ownerKeys.myRestaurant });
    },
  });
}