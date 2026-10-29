import { ApiError, api } from './apiClient';
import type { CreateRestaurantRequest, OwnerRestaurant, RestaurantStatus } from '../types/api';

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

  /**
   * Open or close the doors by hand (US-09).
   *
   * Only the two manual states are accepted; the backend rejects OPEN and
   * CLOSED, which it works out from the schedule itself.
   */
  changeStatus: (restaurantId: string, status: ManualRestaurantStatus) =>
    api.patch<void>(`/api/restaurants/${restaurantId}/status`, { status }),

  /**
   * Drop the manual override so the opening hours decide again (US-09).
   *
   * A delete rather than another status: the owner is not choosing to be open
   * or closed, they are giving the choice back to their schedule.
   */
  followSchedule: (restaurantId: string) =>
    api.delete<void>(`/api/restaurants/${restaurantId}/status`),
};

/** The subset of statuses an owner is allowed to set. */
export type ManualRestaurantStatus = Extract<
  RestaurantStatus,
  'MANUALLY_OPENED' | 'MANUALLY_CLOSED'
>;