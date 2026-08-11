import { Checkbox, FormControlLabel, Stack, Typography } from '@mui/material';
import { Controller, useWatch } from 'react-hook-form';
import type { Control } from 'react-hook-form';

import { FormTextField } from '../../form/FormTextField';
import { WEEK } from './types';
import type { RestaurantFormValues } from './types';
import { humanise } from '../../../utils/format';

interface OpeningHoursFieldsProps {
  control: Control<RestaurantFormValues>;
}

export function OpeningHoursFields({ control }: OpeningHoursFieldsProps) {
  return (
    <Stack spacing={1.5}>
      {WEEK.map((day) => (
        <DayRow key={day} control={control} day={day} />
      ))}
    </Stack>
  );
}

interface DayRowProps {
  control: Control<RestaurantFormValues>;
  day: (typeof WEEK)[number];
}

function DayRow({ control, day }: DayRowProps) {
  // Watching just this day's checkbox so the two time fields can be disabled and
  // exempted from validation while the restaurant is closed.
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
            rules={{ required: 'Required' }}
          />
          <FormTextField
            control={control}
            name={`openingHours.${day}.closed`}
            label="Closes"
            type="time"
            size="small"
            slotProps={{ inputLabel: { shrink: true } }}
            rules={{
              required: 'Required',
              // The backend rejects a range that ends before it starts, so catch
              // it here rather than on submit.
              validate: (value, values) =>
                value > values.openingHours[day].open || 'Must be after opening',
            }}
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