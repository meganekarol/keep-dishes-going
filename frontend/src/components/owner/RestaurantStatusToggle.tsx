import { Alert, Button, Paper, Stack, Typography } from '@mui/material';
import DoorFrontIcon from '@mui/icons-material/DoorFront';

import { useChangeRestaurantStatus } from '../../hooks/useChangeRestaurantStatus';
import type { RestaurantStatus } from '../../types/api';

interface RestaurantStatusToggleProps {
  restaurantId: string;
  status: RestaurantStatus;
}

const EXPLANATION: Record<RestaurantStatus, string> = {
  OPEN: 'Open, following your opening hours.',
  CLOSED: 'Closed, following your opening hours.',
  MANUALLY_OPENED: 'Open because you opened it by hand. Your opening hours are being ignored.',
  MANUALLY_CLOSED: 'Closed because you closed it by hand. Your opening hours are being ignored.',
};

/**
 * Opening early or closing early (US-09).
 *
 * Customers cannot order while the doors are shut, so this takes effect for
 * them as soon as it succeeds.
 */
export function RestaurantStatusToggle({ restaurantId, status }: RestaurantStatusToggleProps) {
  const { mutate, isPending, error } = useChangeRestaurantStatus();

  return (
    <Paper variant="outlined" elevation={0} sx={{ p: 2 }}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { sm: 'center' } }}>
        <DoorFrontIcon color="action" />
        <Typography variant="body2" sx={{ flexGrow: 1 }}>
          {EXPLANATION[status]}
        </Typography>

        <Stack direction="row" spacing={1}>
          <Button
            variant="outlined"
            disabled={isPending || status === 'MANUALLY_OPENED'}
            onClick={() => mutate({ restaurantId, status: 'MANUALLY_OPENED' })}
          >
            Open now
          </Button>
          <Button
            variant="outlined"
            color="warning"
            disabled={isPending || status === 'MANUALLY_CLOSED'}
            onClick={() => mutate({ restaurantId, status: 'MANUALLY_CLOSED' })}
          >
            Close now
          </Button>
        </Stack>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {error instanceof Error ? error.message : 'Could not change the status.'}
        </Alert>
      )}
    </Paper>
  );
}