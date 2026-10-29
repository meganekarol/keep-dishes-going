import { api } from './apiClient';
import type {
  CuisineType,
  DishAvailability,
  DishSortOrder,
  DishType,
  FoodTag,
  MenuItem,
  RestaurantDetail,
  RestaurantSummary,
} from '../types/api';

/** Filters for the catalogue (US-14, US-16). All optional. */
export interface RestaurantFilters {
  cuisineType?: CuisineType | '';
  openNow?: boolean;
  minPrice?: number | '';
  maxPrice?: number | '';
}

/**
 * Where the browser says the customer is. Not a filter: it only sharpens the
 * delivery guesstimate (US-19), and nothing is hidden for being far away.
 */
export interface CustomerPosition {
  lat?: number;
  lon?: number;
}

/** Filters for one restaurant's menu (US-17, US-18). Food tags are AND-combined. */
export interface MenuFilters {
  type?: DishType | '';
  foodTags?: FoodTag[];
  sort?: DishSortOrder | '';
}

export const restaurantService = {
  browse: (filters: RestaurantFilters = {}, position: CustomerPosition = {}) =>
    api.get<RestaurantSummary[]>('/api/restaurants', {
      cuisineType: filters.cuisineType,
      // Only sent when true: `openNow=false` is the server's default anyway.
      openNow: filters.openNow ? true : undefined,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
      lat: position.lat,
      lon: position.lon,
    }),

  getById: (restaurantId: string, position: CustomerPosition = {}) =>
    api.get<RestaurantDetail>(`/api/restaurants/${restaurantId}`, {
      lat: position.lat,
      lon: position.lon,
    }),

  getMenu: (restaurantId: string, filters: MenuFilters = {}) =>
    api.get<MenuItem[]>(`/api/restaurants/${restaurantId}/dishes`, {
      type: filters.type,
      foodTags: filters.foodTags?.length ? filters.foodTags : undefined,
      sort: filters.sort,
    }),

  /**
   * Whether these dishes can still be ordered (US-21). Answers for every id,
   * including ones that have left the menu and so are missing from `getMenu`.
   */
  checkAvailability: (restaurantId: string, dishIds: string[]) =>
    api.get<DishAvailability[]>(`/api/restaurants/${restaurantId}/dishes/availability`, {
      dishIds,
    }),
};
