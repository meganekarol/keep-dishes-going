import { ApiError, api } from './apiClient';
import type { CreateRestaurantRequest, OwnerRestaurant } from '../types/api';

export const ownerService = {
  /**
   * The restaurant belonging to the signed-in owner, or null when they have not
   * created one yet (US-31: an owner has exactly one).
   *
   * The backend answers 404 for "none yet". That is a normal state on a first
   * sign-in, not a failure, so it is translated here instead of being left for
   * every caller to recognise.
   */
  getMyRestaurant: async (): Promise<OwnerRestaurant | null> => {
    try {
      return await api.get<OwnerRestaurant>('/api/restaurants/me');
    } catch (error) {
      if (error instanceof ApiError && error.status === 404) return null;
      throw error;
    }
  },

  /** Create the owner's one restaurant (US-02). */
  createRestaurant: (request: CreateRestaurantRequest) =>
    api.post<OwnerRestaurant>('/api/restaurants', request),
};