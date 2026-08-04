import { useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Chip,
  Divider,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import RadioButtonUncheckedIcon from '@mui/icons-material/RadioButtonUnchecked';

import { QueryState } from '../components/QueryState';
import { useOrderTracking } from '../hooks/useOrderTracking';
import type { OrderStatus, OrderTracking } from '../types/api';
import { formatDateTime, formatPrice, humanise } from '../utils/format';

const STATUS_COLOURS: Record<OrderStatus, 'default' | 'info' | 'success' | 'error' | 'warning'> = {
  PENDING_ACCEPTANCE: 'warning',
  ACCEPTED: 'info',
  REJECTED: 'error',
  READY_FOR_PICKUP: 'info',
  IN_DELIVERY: 'info',
  DELIVERED: 'success',
};

/**
 * Following an order (US-24, US-25).
 *
 * The order id doubles as the link: a guest has no account to bind the order to,
 * so the id they got at checkout is what proves it is theirs. The page polls
 * while the order is in progress — see `useOrderTracking`.
 */
export function OrderTrackingPage() {
  const { orderId = '' } = useParams();
  const { data, isLoading, error } = useOrderTracking(orderId);

  return (
    <QueryState isLoading={isLoading} error={error}>
      {data && (
        <>
          <Stack direction="row" spacing={2} sx={{ mb: 1, alignItems: 'center' }}>
            <Typography variant="h1">Your order</Typography>
            <Chip label={humanise(data.status)} color={STATUS_COLOURS[data.status]} />
          </Stack>

          <Typography color="text.secondary" gutterBottom>
            {data.restaurantName} · placed {formatDateTime(data.placedAt)}
          </Typography>

          {data.status === 'REJECTED' && (
            <Alert severity="error" sx={{ my: 2 }}>
              The restaurant could not take this order
              {data.rejectionReason ? `: ${data.rejectionReason}` : '.'}
            </Alert>
          )}

          <Timeline order={data} />

          <Divider sx={{ my: 3 }} />

          <Typography variant="h2" gutterBottom>
            Items
          </Typography>
          <Paper variant="outlined" elevation={0}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Dish</TableCell>
                  <TableCell align="right">Unit price</TableCell>
                  <TableCell align="right">Qty</TableCell>
                  <TableCell align="right">Subtotal</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.lines.map((line) => (
                  <TableRow key={line.dishId}>
                    <TableCell>{line.dishName}</TableCell>
                    <TableCell align="right">{formatPrice(line.unitPrice)}</TableCell>
                    <TableCell align="right">{line.quantity}</TableCell>
                    <TableCell align="right">{formatPrice(line.subtotal)}</TableCell>
                  </TableRow>
                ))}
                <TableRow>
                  <TableCell colSpan={3} sx={{ fontWeight: 700 }}>
                    Total
                  </TableCell>
                  <TableCell align="right" sx={{ fontWeight: 700 }}>
                    {formatPrice(data.totalPrice)}
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </Paper>

          <Typography variant="h2" sx={{ mt: 3 }} gutterBottom>
            Delivery
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {data.customerName} · {data.deliveryAddress.street} {data.deliveryAddress.number},{' '}
            {data.deliveryAddress.postalCode} {data.deliveryAddress.city}
          </Typography>
        </>
      )}
    </QueryState>
  );
}

/**
 * Built from the timestamps rather than from `status`, because `status` only
 * says where the order is now — the timestamps are what say how it got there,
 * and a rejected order simply never gains the later ones.
 */
function Timeline({ order }: { order: OrderTracking }) {
  const steps = [
    { label: 'Placed', at: order.placedAt },
    { label: 'Accepted', at: order.acceptedAt },
    { label: 'Ready for pickup', at: order.readyAt },
    { label: 'Picked up', at: order.pickedUpAt },
    { label: 'Delivered', at: order.deliveredAt },
  ];

  return (
    <Stack spacing={1.5} sx={{ mt: 2 }}>
      {steps.map((step) => (
        <Stack key={step.label} direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
          {step.at ? (
            <CheckCircleIcon color="success" fontSize="small" />
          ) : (
            <RadioButtonUncheckedIcon color="disabled" fontSize="small" />
          )}
          <Box sx={{ minWidth: 160 }}>
            <Typography variant="body2" color={step.at ? 'text.primary' : 'text.disabled'}>
              {step.label}
            </Typography>
          </Box>
          <Typography variant="body2" color="text.secondary">
            {formatDateTime(step.at)}
          </Typography>
        </Stack>
      ))}
    </Stack>
  );
}
