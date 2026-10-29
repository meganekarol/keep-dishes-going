import { useContext } from 'react';
import { useQuery } from '@tanstack/react-query';

import SecurityContext from '../context/SecurityContext';
import { dishService } from '../services/dishService';

export const dishKeys = {
  all: ['dishes'] as const,
  ownerMenu: (restaurantId: string) => [...dishKeys.all, 'owner', restaurantId] as const,
};

/**
 * The owner's own menu (US-03).
 *
 * Kept under its own key rather than beneath `restaurants`, because the two
 * views of a menu go stale at different times: publishing changes both, editing
 * a draft changes only this one.
 */
export function useOwnerMenu(restaurantId: string) {
  const { isAuthenticated } = useContext(SecurityContext);

  return useQuery({
    queryKey: dishKeys.ownerMenu(restaurantId),
    queryFn: () => dishService.getOwnerMenu(restaurantId),
    enabled: Boolean(restaurantId) && isAuthenticated(),
  });
}