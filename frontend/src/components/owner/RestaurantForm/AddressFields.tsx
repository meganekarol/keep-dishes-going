import { Stack } from '@mui/material';
import type { Control } from 'react-hook-form';

import { FormTextField } from '../../form/FormTextField';
import type { RestaurantFormValues } from './types';

interface AddressFieldsProps {
  control: Control<RestaurantFormValues>;
}

export function AddressFields({ control }: AddressFieldsProps) {
  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <FormTextField
          control={control}
          name="address.street"
          label="Street"
          fullWidth
          rules={{ required: 'Street is required' }}
        />
        <FormTextField
          control={control}
          name="address.number"
          label="Number"
          sx={{ maxWidth: { sm: 140 } }}
          fullWidth
          rules={{
            required: 'Number is required',
            // The backend stores this as an int, so anything else fails there.
            pattern: { value: /^\d+$/, message: 'Digits only' },
          }}
        />
      </Stack>

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
        <FormTextField
          control={control}
          name="address.postalCode"
          label="Postal code"
          sx={{ maxWidth: { sm: 160 } }}
          fullWidth
          rules={{ required: 'Postal code is required' }}
        />
        <FormTextField
          control={control}
          name="address.city"
          label="City"
          fullWidth
          rules={{ required: 'City is required' }}
        />
        <FormTextField
          control={control}
          name="address.country"
          label="Country"
          fullWidth
          rules={{ required: 'Country is required' }}
        />
      </Stack>
    </Stack>
  );
}