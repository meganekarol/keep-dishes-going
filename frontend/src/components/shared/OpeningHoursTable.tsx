import { Paper, Stack, Typography } from '@mui/material';

import type { DayOfWeek, WeeklyOpeningHours } from '../../types/api';
import { formatTime, humanise } from '../../utils/format';

const WEEK: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

interface OpeningHoursTableProps {
  hours: WeeklyOpeningHours;
}

/**
 * The weekly schedule, listed in day order rather than in whatever order the
 * backend's map happened to serialise. Missing days are shown as closed, so a
 * customer looking at a shut restaurant can see when to come back.
 */
export function OpeningHoursTable({ hours }: OpeningHoursTableProps) {
  return (
    <Paper variant="outlined" elevation={0} sx={{ p: 2, maxWidth: 360 }}>
      <Typography variant="subtitle2" gutterBottom>
        Opening hours
      </Typography>
      <Stack spacing={0.5}>
        {WEEK.map((day) => {
          const range = hours[day];
          return (
            <Stack key={day} direction="row" sx={{ justifyContent: 'space-between' }}>
              <Typography variant="body2">{humanise(day)}</Typography>
              <Typography variant="body2" color="text.secondary">
                {range ? `${formatTime(range.open)} to ${formatTime(range.closed)}` : 'Closed'}
              </Typography>
            </Stack>
          );
        })}
      </Stack>
    </Paper>
  );
}