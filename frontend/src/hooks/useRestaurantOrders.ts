import { useContext } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import SecurityContext from '../context/SecurityContext';
import { restaurantOrderService } from '../services/restaurantOrderService';
import type { OrderStatus } from '../types/api';

export const restaurantOrderKeys = {
  all: ['restaurantOrders'] as const,
  list: (restaurantId: string) => [...restaurantOrderKeys.all, restaurantId] as const,
};

const POLL_INTERVAL_MS = 15_000;

/** Orders nobody is waiting on any more, so polling can stop. */
const SETTLED_STATUSES: OrderStatus[] = ['REJECTED', 'DELIVERED'];

export function useRestaurantOrders(restaurantId: string) {
  const { isAuthenticated } = useContext(SecurityContext);

  return useQuery({
    queryKey: restaurantOrderKeys.list(restaurantId),
    queryFn: () => restaurantOrderService.list(restaurantId),
    enabled: Boolean(restaurantId) && isAuthenticated(),
    refetchInterval: (query) => {
      const orders = query.state.data;
      if (orders && orders.every((order) => SETTLED_STATUSES.includes(order.status))) return false;
      return POLL_INTERVAL_MS;
    },
  });
}

type OrderAction = (restaurantId: string, orderId: string) => Promise<void>;

/**
 * The decisions an owner takes on one order (US-10, US-12).
 *
 * Each one refetches the list rather than patching it: accepting also starts
 * the delivery service's side of the story, and the server is the only place
 * that knows what the order looks like afterwards.
 */
function useOrderDecision(restaurantId: string, action: OrderAction) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (orderId: string) => action(restaurantId, orderId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: restaurantOrderKeys.list(restaurantId) });
    },
  });
}

/** Take the order on (US-10). Only possible while it is pending. */
export function useAcceptOrder(restaurantId: string) {
  return useOrderDecision(restaurantId, restaurantOrderService.accept);
}

/** Turn the order away with a reason the customer sees (US-10). */
export function useRejectOrder(restaurantId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ orderId, reason }: { orderId: string; reason: string }) =>
      restaurantOrderService.reject(restaurantId, orderId, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: restaurantOrderKeys.list(restaurantId) });
    },
  });
}

/** Cooked and waiting for a courier (US-12). */
export function useMarkOrderReady(restaurantId: string) {
  return useOrderDecision(restaurantId, restaurantOrderService.markReady);
}
