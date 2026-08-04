import {
  Box,
  Button,
  Checkbox,
  FormControlLabel,
  MenuItem,
  Paper,
  Stack,
  TextField,
} from '@mui/material';
import { Controller, useForm } from 'react-hook-form';

import type { CuisineType } from '../types/api';
import type { RestaurantFilters } from '../services/restaurantService';
import { humanise } from '../utils/format';

const CUISINE_TYPES: CuisineType[] = [
  'FUSION',
  'ITALIAN',
  'FRENCH',
  'JAPANESE',
  'THAI',
  'CHINESE',
  'KOREAN',
  'MEXICAN',
  'INDIAN',
  'BELGIAN',
];

/** Prices are strings while typing, so an empty field stays empty rather than becoming 0. */
interface FormValues {
  cuisineType: CuisineType | '';
  openNow: boolean;
  minPrice: string;
  maxPrice: string;
}

const EMPTY: FormValues = { cuisineType: '', openNow: false, minPrice: '', maxPrice: '' };

export function RestaurantFilterForm({ onApply }: { onApply: (filters: RestaurantFilters) => void }) {
  const { control, handleSubmit, register, reset } = useForm<FormValues>({ defaultValues: EMPTY });

  const submit = (values: FormValues) =>
    onApply({
      cuisineType: values.cuisineType,
      openNow: values.openNow,
      minPrice: values.minPrice === '' ? '' : Number(values.minPrice),
      maxPrice: values.maxPrice === '' ? '' : Number(values.maxPrice),
    });

  const clear = () => {
    reset(EMPTY);
    onApply({});
  };

  return (
    <Paper component="form" onSubmit={handleSubmit(submit)} sx={{ p: 2, mb: 3 }} elevation={0} variant="outlined">
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ alignItems: { md: 'center' } }}>
        <Controller
          name="cuisineType"
          control={control}
          render={({ field }) => (
            <TextField {...field} select label="Cuisine" size="small" sx={{ minWidth: 180 }}>
              <MenuItem value="">Any cuisine</MenuItem>
              {CUISINE_TYPES.map((cuisine) => (
                <MenuItem key={cuisine} value={cuisine}>
                  {humanise(cuisine)}
                </MenuItem>
              ))}
            </TextField>
          )}
        />

        <TextField
          {...register('minPrice')}
          label="Min price"
          type="number"
          size="small"
          slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
          sx={{ maxWidth: 140 }}
        />
        <TextField
          {...register('maxPrice')}
          label="Max price"
          type="number"
          size="small"
          slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
          sx={{ maxWidth: 140 }}
        />

        <Controller
          name="openNow"
          control={control}
          render={({ field }) => (
            <FormControlLabel
              control={<Checkbox {...field} checked={field.value} />}
              label="Open now"
            />
          )}
        />

        <Box sx={{ flexGrow: 1 }} />

        <Stack direction="row" spacing={1}>
          <Button type="submit" variant="contained">
            Apply
          </Button>
          <Button type="button" onClick={clear}>
            Clear
          </Button>
        </Stack>
      </Stack>
    </Paper>
  );
}
