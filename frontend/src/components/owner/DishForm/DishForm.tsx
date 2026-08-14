import { Link } from 'react-router-dom';
import { Alert, Box, Button, MenuItem, Paper, Stack, Typography } from '@mui/material';
import { useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';

import { FoodTagFields } from './FoodTagFields';
import { DISH_TYPES } from './types';
import type { DishFormValues } from './types';
import { FormTextField } from '../../form/FormTextField';
import { humanise } from '../../../utils/format';

interface DishFormProps {
  defaultValues: DishFormValues;
  onSubmit: SubmitHandler<DishFormValues>;
  isSubmitting: boolean;
  submitLabel: string;
  error?: string;
}

/**
 * The form behind both adding a dish and editing one (US-03).
 *
 * The two take the same fields, because an edit replaces the draft wholesale
 * rather than patching single fields.
 */
export function DishForm({
  defaultValues,
  onSubmit,
  isSubmitting,
  submitLabel,
  error,
}: DishFormProps) {
  const { control, handleSubmit } = useForm<DishFormValues>({ defaultValues });

  return (
    <Paper
      component="form"
      variant="outlined"
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      sx={{ p: 3 }}
    >
      <Stack spacing={3}>
        <Stack spacing={2}>
          <FormTextField
            control={control}
            name="name"
            label="Dish name"
            fullWidth
            rules={{ required: 'A name is required' }}
          />

          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
            <FormTextField
              control={control}
              name="type"
              label="Course"
              select
              fullWidth
              rules={{ required: 'Pick a course' }}
            >
              {DISH_TYPES.map((type) => (
                <MenuItem key={type} value={type}>
                  {humanise(type)}
                </MenuItem>
              ))}
            </FormTextField>

            <FormTextField
              control={control}
              name="price"
              label="Price"
              type="number"
              fullWidth
              slotProps={{ htmlInput: { min: 0, step: '0.01' } }}
              rules={{
                required: 'A price is required',
                min: { value: 0.01, message: 'A dish has to cost something' },
              }}
            />
          </Stack>

          <FormTextField
            control={control}
            name="description"
            label="Description"
            fullWidth
            multiline
            rows={3}
            helperText="What customers read on the menu"
            rules={{ required: 'A description is required' }}
          />

          <FormTextField
            control={control}
            name="pictureUrl"
            label="Picture URL"
            fullWidth
            helperText="Optional"
            rules={{
              pattern: { value: /^https?:\/\/.+/, message: 'Must start with http:// or https://' },
            }}
          />
        </Stack>

        <Box>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Dietary tags
          </Typography>
          <FoodTagFields control={control} />
        </Box>

        {error && <Alert severity="error">{error}</Alert>}

        <Stack direction="row" spacing={2}>
          <Button type="submit" variant="contained" size="large" disabled={isSubmitting}>
            {isSubmitting ? 'Saving' : submitLabel}
          </Button>
          <Button component={Link} to="/owner/menu" size="large" disabled={isSubmitting}>
            Cancel
          </Button>
        </Stack>
      </Stack>
    </Paper>
  );
}