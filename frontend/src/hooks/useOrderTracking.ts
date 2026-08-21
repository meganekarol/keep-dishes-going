import { useMutation, useQuery } from '@tanstack/react-query';
import { ApiError } from '../services/apiClient';
import { orderService } from '../services/orderService';
import type { PlaceOrderRequest } from '../services/orderService';
import type { OrderStatus } from '../types/api';

export const orderKeys = {
  all: ['orders'] as const,
  tracking: (orderId: string) => [...orderKeys.all, 'tracking', orderId] as const,
};

const POLL_INTERVAL_MS = 5000;

const TERMINAL_STATUSES: OrderStatus[] = ['REJECTED', 'DELIVERED', 'PAYMENT_EXPIRED'];

/**
 * Follow an order from placement to delivery (US-24, US-25).
 *
 * The restaurant accepts, prepares and hands over the order in its own time and
 * has no way to push that to the browser, so the page polls. Polling stops once
 * the order reaches a terminal state nothing can change after that, and a
 * tracking page left open overnight should not keep hitting the API.
 *
 * @param pollEvery milliseconds between refreshes. The default suits one order
 *                  on screen; a list of them should ask for a slower rate,
 *                  since every order on it polls on its own.
 */
export function useOrderTracking(orderId: string, pollEvery: number = POLL_INTERVAL_MS) {
  return useQuery({
    queryKey: orderKeys.tracking(orderId),
    queryFn: () => orderService.track(orderId),
    enabled: Boolean(orderId),
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      if (status && TERMINAL_STATUSES.includes(status)) return false;
      return pollEvery;
    },
    // A mistyped order id answers 404 and will answer 404 again; only retry
    // what might genuinely be transient.
    retry: (failureCount, error) => {
      if (error instanceof ApiError && error.status < 500) return false;
      return failureCount < 2;
    },
  });
}

/** Checkout (US-20, US-21, US-30 guests order without an account). */
export function usePlaceOrder() {
  return useMutation({
    mutationFn: (request: PlaceOrderRequest) => orderService.place(request),
  });
}
