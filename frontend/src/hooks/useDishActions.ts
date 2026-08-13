import { useMutation, useQueryClient } from '@tanstack/react-query';

import { dishKeys } from './useOwnerMenu';
import { restaurantKeys } from './useRestaurants';
import { dishService } from '../services/dishService';

type DishAction = (restaurantId: string, dishId: string) => Promise<void>;

/**
 * The four state changes a single dish can go through (US-04, US-05, US-08).
 *
 * All of them reach the customer's menu, so the public catalogue is invalidated
 * alongside the owner's own view. Editing a draft does not, which is why that
 * one lives elsewhere.
 */
function useDishStateChange(restaurantId: string, action: DishAction) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (dishId: string) => action(restaurantId, dishId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: dishKeys.ownerMenu(restaurantId) });
      queryClient.invalidateQueries({ queryKey: restaurantKeys.all });
    },
  });
}

/** Put the draft live (US-04). Fails on the cap, or with no draft to publish. */
export function usePublishDish(restaurantId: string) {
  return useDishStateChange(restaurantId, dishService.publish);
}

/** Take a dish off the menu (US-05). Fails while the dish has a draft. */
export function useUnpublishDish(restaurantId: string) {
  return useDishStateChange(restaurantId, dishService.unpublish);
}

/** Sold out, but staying on the menu greyed out (US-08). */
export function useMarkDishOutOfStock(restaurantId: string) {
  return useDishStateChange(restaurantId, dishService.markOutOfStock);
}

/** Back in stock (US-08). */
export function useMarkDishInStock(restaurantId: string) {
  return useDishStateChange(restaurantId, dishService.markInStock);
}