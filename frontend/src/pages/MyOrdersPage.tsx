import { Link as RouterLink } from 'react-router-dom';
import {
  Alert,
  Button,
  Card,
  CardActionArea,
  CardContent,
  Chip,
  Stack,
  Typography,
} from '@mui/material';

import { QueryState } from '../components/shared/QueryState';
import { usePlacedOrders } from '../hooks/usePlacedOrders';
import { useOrderTracking } from '../hooks/useOrderTracking';
import type { OrderStatus } from '../types/api';
import { formatDateTime, formatPrice, humanise } from '../utils/format';

const LIST_POLL_MS = 30_000;

const STATUS_COLOURS: Record<OrderStatus, 'default' | 'info' | 'success' | 'error' | 'warning'> = {
  PENDING_ACCEPTANCE: 'warning',
  ACCEPTED: 'info',
  REJECTED: 'error',
  READY_FOR_PICKUP: 'info',
  IN_DELIVERY: 'info',
  DELIVERED: 'success',
};

/**
 * Every order this browser has placed (US-24).
 *
 * A guest has no account, so this is the only way back to an order once the
 * tab that placed it is gone.
 */
export function MyOrdersPage() {
  const { orders } = usePlacedOrders();

  return (
    <>
      <Typography variant="h1" gutterBottom>
        My orders
      </Typography>

      {orders.length === 0 ? (
        <>
          <Alert severity="info" sx={{ mb: 2 }}>
            You have not ordered anything from this browser yet.
          </Alert>
          <Button variant="contained" component={RouterLink} to="/restaurants">
            Find a restaurant
          </Button>
        </>
      ) : (
        <>
          <Typography color="text.secondary" gutterBottom>
            Kept on this device. Bookmark an order to reach it from anywhere else.
          </Typography>

          <Stack spacing={2} sx={{ mt: 2 }}>
            {orders.map((order) => (
              <OrderRow key={order.orderId} orderId={order.orderId} placedAt={order.placedAt} />
            ))}
          </Stack>
        </>
      )}
    </>
  );
}

/**
 * One remembered order, with its status read from the server.
 *
 * The status is never stored alongside the id: it changes in the kitchen, and
 * a copy kept here would be stale the moment the restaurant touched it.
 */
function OrderRow({ orderId, placedAt }: { orderId: string; placedAt: string }) {

  const { data, isLoading, error } = useOrderTracking(orderId, LIST_POLL_MS);

  return (
    <Card variant="outlined">
      <CardActionArea component={RouterLink} to={`/orders/${orderId}`}>
        <CardContent>
          <QueryState isLoading={isLoading} error={error}>
            {data && (
              <>
                <Stack
                  direction={{ xs: 'column', sm: 'row' }}
                  spacing={1}
                  sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
                >
                  <Typography sx={{ fontWeight: 600 }}>{data.restaurantName}</Typography>
                  <Chip
                    size="small"
                    label={humanise(data.status)}
                    color={STATUS_COLOURS[data.status]}
                  />
                </Stack>

                <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                  {formatPrice(data.totalPrice)} · placed {formatDateTime(placedAt)}
                </Typography>
              </>
            )}
          </QueryState>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}