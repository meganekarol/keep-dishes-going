import { Alert, Button, Paper, Stack, Typography } from '@mui/material';
import DoorFrontIcon from '@mui/icons-material/DoorFront';

import { useChangeRestaurantStatus, useFollowSchedule } from '../../hooks/useChangeRestaurantStatus';
import type { RestaurantStatus } from '../../types/api';

interface RestaurantStatusToggleProps {
  restaurantId: string;
  status: RestaurantStatus;
}

const EXPLANATION: Record<RestaurantStatus, string> = {
  OPEN: 'Open, following your opening hours.',
  CLOSED: 'Closed, following your opening hours.',
  MANUALLY_OPENED: 'Open because you opened it by hand, until you go back to your hours.',
  MANUALLY_CLOSED: 'Closed because you closed it by hand, until you go back to your hours.',
};

function isOverridden(status: RestaurantStatus) {
  return status === 'MANUALLY_OPENED' || status === 'MANUALLY_CLOSED';
}

/**
 * Opening early or closing early (US-09).
 *
 * Customers cannot order while the doors are shut, so this takes effect for
 * them as soon as it succeeds. An override lasts until it is given back, which
 * is what the third button is for.
 */
export function RestaurantStatusToggle({ restaurantId, status }: RestaurantStatusToggleProps) {
  const override = useChangeRestaurantStatus();
  const schedule = useFollowSchedule();

  const busy = override.isPending || schedule.isPending;
  const failure = override.error ?? schedule.error;

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
            disabled={busy || status === 'MANUALLY_OPENED'}
            onClick={() => override.mutate({ restaurantId, status: 'MANUALLY_OPENED' })}
          >
            Open now
          </Button>
          <Button
            variant="outlined"
            color="warning"
            disabled={busy || status === 'MANUALLY_CLOSED'}
            onClick={() => override.mutate({ restaurantId, status: 'MANUALLY_CLOSED' })}
          >
            Close now
          </Button>
          <Button
            disabled={busy || !isOverridden(status)}
            onClick={() => schedule.mutate(restaurantId)}
          >
            Follow my hours
          </Button>
        </Stack>
      </Stack>

      {failure && (
        <Alert severity="error" sx={{ mt: 2 }}>
          {failure instanceof Error ? failure.message : 'Could not change the status.'}
        </Alert>
      )}
    </Paper>
  );
}