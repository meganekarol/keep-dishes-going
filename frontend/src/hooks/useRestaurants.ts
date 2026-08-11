import { useQuery } from '@tanstack/react-query';
import { restaurantService } from '../services/restaurantService';
import type { MenuFilters, RestaurantFilters } from '../services/restaurantService';

export const restaurantKeys = {
  all: ['restaurants'] as const,
  list: (filters: RestaurantFilters) => [...restaurantKeys.all, 'list', filters] as const,
  detail: (restaurantId: string) => [...restaurantKeys.all, 'detail', restaurantId] as const,
  menu: (restaurantId: string, filters: MenuFilters) =>
    [...restaurantKeys.all, 'menu', restaurantId, filters] as const,
};

/** The catalogue (US-14, US-16). Refetches whenever the filters change. */
export function useRestaurants(filters: RestaurantFilters = {}) {
  return useQuery({
    queryKey: restaurantKeys.list(filters),
    queryFn: () => restaurantService.browse(filters),
  });
}

/** One restaurant with its full menu (US-15). */
export function useRestaurant(restaurantId: string) {
  return useQuery({
    queryKey: restaurantKeys.detail(restaurantId),
    queryFn: () => restaurantService.getById(restaurantId),
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
