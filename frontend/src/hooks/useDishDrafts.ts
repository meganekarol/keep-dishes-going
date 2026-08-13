import { useMutation, useQueryClient } from '@tanstack/react-query';

import { dishKeys } from './useOwnerMenu';
import { dishService } from '../services/dishService';
import type { CreateDishRequest, EditDishRequest } from '../types/api';

interface EditDraftVariables {
  dishId: string;
  request: EditDishRequest;
}

/**
 * Add a dish (US-03).
 *
 * Only the owner's view is invalidated: a new dish starts as a draft, so
 * nothing on the customer's menu has moved yet.
 */
export function useCreateDish(restaurantId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateDishRequest) => dishService.create(restaurantId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dishKeys.ownerMenu(restaurantId) });
    },
  });
}

/**
 * Park an edit as a draft (US-03).
 *
 * Same reasoning as creating one: customers keep ordering the published version
 * until the draft is published in its turn.
 */
export function useEditDishDraft(restaurantId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ dishId, request }: EditDraftVariables) =>
      dishService.editDraft(restaurantId, dishId, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dishKeys.ownerMenu(restaurantId) });
    },
  });
}