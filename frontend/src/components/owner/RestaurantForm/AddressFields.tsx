import { Stack } from '@mui/material';
import { useFormContext } from 'react-hook-form';

import { FormTextField } from '../../form/FormTextField';
import type { RestaurantFormValues } from './types';

export function AddressFields() {
  const { control } = useFormContext<RestaurantFormValues>();

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <FormTextField control={control} name="address.street" label="Street" fullWidth />
        <FormTextField
          control={control}
          name="address.number"
          label="Number"
          sx={{ maxWidth: { sm: 140 } }}
          fullWidth
        />
      </Stack>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <FormTextField
          control={control}
          name="address.postalCode"
          label="Postal code"
          sx={{ maxWidth: { sm: 160 } }}
          fullWidth
        />
        <FormTextField control={control} name="address.city" label="City" fullWidth />
        <FormTextField control={control} name="address.country" label="Country" fullWidth />
      </Stack>
    </Stack>
  );
}
