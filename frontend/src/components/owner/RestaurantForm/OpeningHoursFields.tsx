import { Checkbox, FormControlLabel, Stack, Typography } from '@mui/material';
import { Controller, useFormContext, useWatch } from 'react-hook-form';

import { FormTextField } from '../../form/FormTextField';
import { WEEK } from './types';
import type { RestaurantFormValues } from './types';
import { humanise } from '../../../utils/format';

export function OpeningHoursFields() {
  return (
    <Stack spacing={1.5}>
      {WEEK.map((day) => (
        <DayRow key={day} day={day} />
      ))}
    </Stack>
  );
}

function DayRow({ day }: { day: (typeof WEEK)[number] }) {
  const { control } = useFormContext<RestaurantFormValues>();

  // Watching just this day's checkbox so the two time fields can be hidden while
  // the restaurant is closed. The schema skips their check on a closed day.
  const enabled = useWatch({ control, name: `openingHours.${day}.enabled` });

  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { sm: 'center' } }}>
      <Controller
        control={control}
        name={`openingHours.${day}.enabled`}
        render={({ field }) => (
          <FormControlLabel
            sx={{ minWidth: 160 }}
            control={<Checkbox {...field} checked={field.value} />}
            label={humanise(day)}
          />
        )}
      />

      {enabled ? (
        <Stack direction="row" spacing={2}>
          <FormTextField
            control={control}
            name={`openingHours.${day}.open`}
            label="Opens"
            type="time"
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
          />
          <FormTextField
            control={control}
            name={`openingHours.${day}.closed`}
            label="Closes"
            type="time"
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
          />
        </Stack>
      ) : (
        <Typography variant="body2" color="text.secondary">
          Closed
        </Typography>
      )}
    </Stack>
  );
}
