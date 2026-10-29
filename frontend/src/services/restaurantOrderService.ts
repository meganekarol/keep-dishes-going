import { api } from './apiClient';
import type { RestaurantOrderSummary } from '../types/api';

/**
 * What an owner can do with the orders placed at their restaurant (US-10, US-12).
 *
 * Every path carries the restaurant id because the backend checks the caller
 * owns that restaurant, not merely that they are an owner.
 */
export const restaurantOrderService = {
  list: (restaurantId: string) =>
    api.get<RestaurantOrderSummary[]>(`/api/restaurants/${restaurantId}/orders`),

  accept: (restaurantId: string, orderId: string) =>
    api.post<void>(`/api/restaurants/${restaurantId}/orders/${orderId}/accept`),

  /** The reason is passed on to the customer, so it is never left blank. */
  reject: (restaurantId: string, orderId: string, reason: string) =>
    api.post<void>(`/api/restaurants/${restaurantId}/orders/${orderId}/reject`, { reason }),

  markReady: (restaurantId: string, orderId: string) =>
    api.post<void>(`/api/restaurants/${restaurantId}/orders/${orderId}/ready`),
};
