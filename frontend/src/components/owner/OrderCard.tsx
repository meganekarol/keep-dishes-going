import { useState } from 'react';
import {
  Alert,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
  Typography,
} from '@mui/material';

import type { OrderStatus, RestaurantOrderSummary } from '../../types/api';
import { formatDateTime, formatPrice } from '../../utils/format';

type ChipColour = 'default' | 'warning' | 'success' | 'error' | 'info';

const STATUS_COLOURS: Record<OrderStatus, ChipColour> = {
  PENDING_ACCEPTANCE: 'warning',
  ACCEPTED: 'info',
  REJECTED: 'error',
  READY_FOR_PICKUP: 'success',
  IN_DELIVERY: 'info',
  DELIVERED: 'default',
};

const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING_ACCEPTANCE: 'Waiting for you',
  ACCEPTED: 'Cooking',
  REJECTED: 'Turned away',
  READY_FOR_PICKUP: 'Waiting for a courier',
  IN_DELIVERY: 'On its way',
  DELIVERED: 'Delivered',
};

interface OrderCardProps {
  order: RestaurantOrderSummary;
  onAccept: (orderId: string) => void;
  onReject: (orderId: string, reason: string) => void;
  onMarkReady: (orderId: string) => void;
  isBusy: boolean;
  error: Error | null;
}

/**
 * One order, with whatever the kitchen can do about it right now (US-10, US-12).
 *
 * The buttons follow the same rules the backend enforces, so a button that is
 * shown is one the server will accept.
 */
export function OrderCard({ order, onAccept, onReject, onMarkReady, isBusy, error }: OrderCardProps) {
  const [rejecting, setRejecting] = useState(false);
  const [reason, setReason] = useState('');

  const isPending = order.status === 'PENDING_ACCEPTANCE';
  const isAccepted = order.status === 'ACCEPTED';

  const confirmRejection = () => {
    onReject(order.orderId, reason.trim());
    setRejecting(false);
    setReason('');
  };

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack
          direction={{ xs: 'column', sm: 'row' }}
          spacing={1}
          sx={{ justifyContent: 'space-between', alignItems: { sm: 'flex-start' } }}
        >
          <div>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
              {order.customerName}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {order.deliveryAddress}
            </Typography>
          </div>
          <Chip
            size="small"
            color={STATUS_COLOURS[order.status]}
            label={STATUS_LABELS[order.status]}
          />
        </Stack>

        <Typography sx={{ mt: 1.5 }}>{order.itemSummary}</Typography>

        <Stack direction="row" spacing={2} sx={{ mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            {order.itemCount} {order.itemCount === 1 ? 'dish' : 'dishes'}
          </Typography>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {formatPrice(order.totalPrice)}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Placed {formatDateTime(order.placedAt)}
          </Typography>
        </Stack>

        {order.rejectionReason && (
          <Alert severity="info" sx={{ mt: 1.5 }}>
            You turned this one away: {order.rejectionReason}
          </Alert>
        )}

        {isPending && (
          <Alert severity="warning" sx={{ mt: 1.5 }}>
            Orders you leave alone are turned away automatically five minutes after they arrive.
          </Alert>
        )}

        {error && (
          <Alert severity="error" sx={{ mt: 1.5 }}>
            {error.message}
          </Alert>
        )}
      </CardContent>

      {(isPending || isAccepted) && (
        <CardActions>
          {isPending && (
            <>
              <Button
                variant="contained"
                disabled={isBusy}
                onClick={() => onAccept(order.orderId)}
              >
                Accept
              </Button>
              <Button color="error" disabled={isBusy} onClick={() => setRejecting(true)}>
                Turn away
              </Button>
            </>
          )}
          {isAccepted && (
            <Button
              variant="contained"
              disabled={isBusy}
              onClick={() => onMarkReady(order.orderId)}
            >
              Ready for pickup
            </Button>
          )}
        </CardActions>
      )}

      <Dialog open={rejecting} onClose={() => setRejecting(false)} fullWidth maxWidth="sm">
        <DialogTitle>Turn away {order.customerName}&apos;s order?</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            fullWidth
            multiline
            rows={3}
            margin="dense"
            label="Why?"
            // The customer reads this on their tracking page, so an empty
            // reason would leave them turned away with no explanation.
            helperText="The customer sees this on their order."
            value={reason}
            onChange={(event) => setReason(event.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRejecting(false)}>Keep the order</Button>
          <Button color="error" variant="contained" disabled={!reason.trim()} onClick={confirmRejection}>
            Turn it away
          </Button>
        </DialogActions>
      </Dialog>
    </Card>
  );
}