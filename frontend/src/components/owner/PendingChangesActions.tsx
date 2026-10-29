import { useState } from 'react';
import { Alert, Button, Stack, TextField, Tooltip } from '@mui/material';
import PublishIcon from '@mui/icons-material/Publish';
import ScheduleIcon from '@mui/icons-material/Schedule';

import { useApplyPendingChanges, useScheduleChanges } from '../../hooks/useMenuChanges';
import type { OwnerMenu } from '../../types/api';
import { formatDateTime } from '../../utils/format';

interface PendingChangesActionsProps {
  restaurantId: string;
  menu: OwnerMenu;
}

/**
 * Publishing every waiting change at once, now or later (US-06, US-07).
 *
 * Both act on the whole restaurant rather than one dish, so they live above the
 * list instead of on a card. The backend applies the batch in one transaction,
 * which is why the cap is checked here first: one dish over and none of them
 * would go live.
 */
export function PendingChangesActions({ restaurantId, menu }: PendingChangesActionsProps) {
  const [scheduledTime, setScheduledTime] = useState('');
  const [showSchedule, setShowSchedule] = useState(false);

  const applyNow = useApplyPendingChanges(restaurantId);
  const scheduleLater = useScheduleChanges(restaurantId);

  const busy = applyNow.isPending || scheduleLater.isPending;
  const failure = applyNow.error ?? scheduleLater.error;

  if (menu.pendingChangesCount === 0) return null;

  // The batch takes every dish holding a draft, and only the ones that are not
  // already on the menu need a slot of their own.
  const additions = menu.dishes.filter(
    (dish) => dish.draft !== null && dish.status !== 'PUBLISHED' && dish.status !== 'OUT_OF_STOCK',
  ).length;
  const wouldOverflow = menu.publishedCount + additions > menu.maxPublishedDishes;

  const alreadyScheduled = menu.dishes.find((dish) => dish.scheduledPublishAt)?.scheduledPublishAt;
  const inThePast = scheduledTime !== '' && new Date(scheduledTime) <= new Date();

  const submitSchedule = () => {
    // Seconds spelled out: the picker leaves them off and the backend reads a
    // full local date and time.
    scheduleLater.mutate(`${scheduledTime}:00`, {
      onSuccess: () => {
        setShowSchedule(false);
        setScheduledTime('');
      },
    });
  };

  return (
    <Alert severity="warning" icon={false}>
      <Stack spacing={2}>
        <span>
          {menu.pendingChangesCount === 1 ? '1 dish has' : `${menu.pendingChangesCount} dishes have`}{' '}
          changes that customers cannot see yet.
          {alreadyScheduled && ` Going live on ${formatDateTime(alreadyScheduled)}.`}
        </span>

        <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
          <Tooltip
            title={
              wouldOverflow
                ? `That would put ${menu.publishedCount + additions} dishes on a menu that holds ${menu.maxPublishedDishes}. Nothing would go live.`
                : ''
            }
          >
            <span>
              <Button
                size="small"
                variant="contained"
                startIcon={<PublishIcon />}
                disabled={busy || wouldOverflow}
                onClick={() => applyNow.mutate()}
              >
                Publish everything now
              </Button>
            </span>
          </Tooltip>

          <Button
            size="small"
            startIcon={<ScheduleIcon />}
            disabled={busy}
            onClick={() => setShowSchedule((open) => !open)}
          >
            {showSchedule ? 'Cancel' : 'Schedule for later'}
          </Button>
        </Stack>

        {showSchedule && (
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1} sx={{ alignItems: 'flex-start' }}>
            <TextField
              size="small"
              type="datetime-local"
              label="Go live at"
              value={scheduledTime}
              onChange={(event) => setScheduledTime(event.target.value)}
              error={inThePast}
              helperText={inThePast ? 'Pick a moment still to come.' : ' '}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <Button
              size="small"
              variant="contained"
              sx={{ mt: 0.5 }}
              disabled={busy || scheduledTime === '' || inThePast}
              onClick={submitSchedule}
            >
              Schedule
            </Button>
          </Stack>
        )}

        {failure instanceof Error && <Alert severity="error">{failure.message}</Alert>}
      </Stack>
    </Alert>
  );
}
