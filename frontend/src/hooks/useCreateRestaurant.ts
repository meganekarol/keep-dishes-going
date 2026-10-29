import { useMutation, useQueryClient } from '@tanstack/react-query';

import { ownerKeys } from './useMyRestaurant';
import { ownerService } from '../services/ownerService';

/**
 * Creates the owner's restaurant (US-02).
 */
export function useCreateRestaurant() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ownerService.createRestaurant,
    onSuccess: (restaurant) => {
      queryClient.setQueryData(ownerKeys.myRestaurant, restaurant);
    },
  });
}