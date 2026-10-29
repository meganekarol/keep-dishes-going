import { Link, Navigate } from 'react-router-dom';
import { Alert, Button, Divider, Stack, Typography } from '@mui/material';

import { OrderCard } from '../../components/owner/OrderCard';
import { QueryState } from '../../components/shared/QueryState';
import { useMyRestaurant } from '../../hooks/useMyRestaurant';
import {
  useAcceptOrder,
  useMarkOrderReady,
  useRejectOrder,
  useRestaurantOrders,
} from '../../hooks/useRestaurantOrders';
import type { OrderStatus } from '../../types/api';

/** Orders that are done with, kept apart from the ones still needing attention. */
const FINISHED: OrderStatus[] = ['REJECTED', 'DELIVERED'];

/**
 * The kitchen's screen: incoming orders and what to do with them (US-10, US-12).
 */
export function OwnerOrdersPage() {
  const restaurantQuery = useMyRestaurant();
  const restaurantId = restaurantQuery.data?.restaurantId ?? '';

  const ordersQuery = useRestaurantOrders(restaurantId);
  const accept = useAcceptOrder(restaurantId);
  const reject = useRejectOrder(restaurantId);
  const markReady = useMarkOrderReady(restaurantId);

  if (!restaurantQuery.isLoading && !restaurantQuery.error && restaurantQuery.data === null) {
    return <Navigate to="/owner/restaurant/new" replace />;
  }

  const isBusy = accept.isPending || reject.isPending || markReady.isPending;


  const failed = accept.error
    ? accept.variables
    : reject.error
      ? reject.variables?.orderId
      : markReady.error
        ? markReady.variables
        : undefined;
  const error = accept.error ?? reject.error ?? markReady.error;

  const orders = ordersQuery.data ?? [];
  const active = orders.filter((order) => !FINISHED.includes(order.status));
  const finished = orders.filter((order) => FINISHED.includes(order.status));

  return (
    <Stack spacing={3}>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
      >
        <Typography variant="h1">Orders</Typography>
        <Button component={Link} to="/owner">
          Back to my restaurant
        </Button>
      </Stack>

      <QueryState isLoading={ordersQuery.isLoading} error={ordersQuery.error}>
        {orders.length === 0 ? (
          <Alert severity="info">No orders yet. This list updates itself as they come in.</Alert>
        ) : (
          <Stack spacing={3}>
            <Stack spacing={2}>
              {active.length === 0 ? (
                <Alert severity="success">Nothing waiting on you right now.</Alert>
              ) : (
                active.map((order) => (
                  <OrderCard
                    key={order.orderId}
                    order={order}
                    onAccept={accept.mutate}
                    onReject={(orderId, reason) => reject.mutate({ orderId, reason })}
                    onMarkReady={markReady.mutate}
                    isBusy={isBusy}
                    error={order.orderId === failed ? error : null}
                  />
                ))
              )}
            </Stack>

            {finished.length > 0 && (
              <>
                <Divider />
                <Typography variant="h2">Earlier</Typography>
                <Stack spacing={2}>
                  {finished.map((order) => (
                    <OrderCard
                      key={order.orderId}
                      order={order}
                      onAccept={accept.mutate}
                      onReject={(orderId, reason) => reject.mutate({ orderId, reason })}
                      onMarkReady={markReady.mutate}
                      isBusy={isBusy}
                      error={null}
                    />
                  ))}
                </Stack>
              </>
            )}
          </Stack>
        )}
      </QueryState>
    </Stack>
  );
}
