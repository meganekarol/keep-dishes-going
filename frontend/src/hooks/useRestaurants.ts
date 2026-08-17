import { useQuery } from '@tanstack/react-query';
import { restaurantService } from '../services/restaurantService';
import type { CustomerPosition, MenuFilters, RestaurantFilters } from '../services/restaurantService';
import { useCustomerLocation } from './useCustomerLocation';

export const restaurantKeys = {
  all: ['restaurants'] as const,
  list: (filters: RestaurantFilters, position: CustomerPosition) =>
    [...restaurantKeys.all, 'list', filters, position] as const,
  detail: (restaurantId: string, position: CustomerPosition) =>
    [...restaurantKeys.all, 'detail', restaurantId, position] as const,
  menu: (restaurantId: string, filters: MenuFilters) =>
    [...restaurantKeys.all, 'menu', restaurantId, filters] as const,
};

/**
 * The catalogue (US-14, US-16). Refetches whenever the filters change.
 *
 * The position is part of the key, so the list is fetched once without it and
 * again once the customer allows the location prompt, with sharper estimates.
 */
export function useRestaurants(filters: RestaurantFilters = {}) {
  const { location } = useCustomerLocation();
  const position: CustomerPosition = location ?? {};

  return useQuery({
    queryKey: restaurantKeys.list(filters, position),
    queryFn: () => restaurantService.browse(filters, position),
  });
}

/** One restaurant with its full menu (US-15). */
export function useRestaurant(restaurantId: string) {
  const { location } = useCustomerLocation();
  const position: CustomerPosition = location ?? {};

  return useQuery({
    queryKey: restaurantKeys.detail(restaurantId, position),
    queryFn: () => restaurantService.getById(restaurantId, position),
    enabled: Boolean(restaurantId),
  });
}

/**
 * The same menu, filtered and sorted server-side (US-17, US-18).
 *
 * Sorting stays on the server rather than in the component so the list the
 * customer sees is the list the backend defined the price comparators live in
 * `DishSortOrder` and there is no second copy of them here.
 */
export function useMenu(restaurantId: string, filters: MenuFilters = {}) {
  return useQuery({
    queryKey: restaurantKeys.menu(restaurantId, filters),
    queryFn: () => restaurantService.getMenu(restaurantId, filters),
    enabled: Boolean(restaurantId),
  });
}
