import { api } from './apiClient';
import type {
  CuisineType,
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

/** Filters for one restaurant's menu (US-17, US-18). Food tags are AND-combined. */
export interface MenuFilters {
  type?: DishType | '';
  foodTags?: FoodTag[];
  sort?: DishSortOrder | '';
}

export const restaurantService = {
  browse: (filters: RestaurantFilters = {}) =>
    api.get<RestaurantSummary[]>('/api/restaurants', {
      cuisineType: filters.cuisineType,
      // Only sent when true: `openNow=false` is the server's default anyway.
      openNow: filters.openNow ? true : undefined,
      minPrice: filters.minPrice,
      maxPrice: filters.maxPrice,
    }),

  getById: (restaurantId: string) =>
    api.get<RestaurantDetail>(`/api/restaurants/${restaurantId}`),

  getMenu: (restaurantId: string, filters: MenuFilters = {}) =>
    api.get<MenuItem[]>(`/api/restaurants/${restaurantId}/dishes`, {
      type: filters.type,
      foodTags: filters.foodTags?.length ? filters.foodTags : undefined,
      sort: filters.sort,
    }),
};
