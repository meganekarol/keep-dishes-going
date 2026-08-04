import { api } from './apiClient';
import type { OrderTracking } from '../types/api';

/**
 * What the customer fills in at checkout. Only dish ids and quantities are sent:
 * the backend resolves names and prices itself, so the client cannot influence
 * what the order costs.
 */
export interface PlaceOrderRequest {
  restaurantId: string;
  customerName: string;
  customerEmail: string;
  deliveryStreet: string;
  deliveryNumber: string;
  deliveryPostalCode: string;
  deliveryCity: string;
  deliveryCountry: string;
  items: { dishId: string; quantity: number }[];
}

export interface PlaceOrderResponse {
  orderId: string;
  message: string;
}

export const orderService = {
  place: (request: PlaceOrderRequest) =>
    api.post<PlaceOrderResponse>('/api/orders', request),

  /** The order id from checkout is both the confirmation and the tracking handle. */
  track: (orderId: string) => api.get<OrderTracking>(`/api/orders/${orderId}`),
};
