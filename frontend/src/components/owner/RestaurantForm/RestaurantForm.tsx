import type { ReactNode } from 'react';
import { Alert, Box, Button, Divider, MenuItem, Paper, Stack, Typography } from '@mui/material';
import { FormProvider, useForm } from 'react-hook-form';
import type { SubmitHandler } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

import { AddressFields } from './AddressFields';
import { OpeningHoursFields } from './OpeningHoursFields';
import { PictureUrlFields } from './PictureUrlFields';
import { CUISINE_TYPES, EMPTY_RESTAURANT_FORM, WEEK, restaurantFormSchema } from './types';
import type { RestaurantFormValues } from './types';
import { FormTextField } from '../../form/FormTextField';
import { humanise } from '../../../utils/format';

interface RestaurantFormProps {
  onSubmit: SubmitHandler<RestaurantFormValues>;
  isSubmitting: boolean;
  error?: string;
}

/**
 * The create restaurant form (US-02), collecting exactly the fields the brief
 * lists.
 *
 * It only gathers and validates input. Turning the values into a request and
 * sending it belongs to whoever renders this.
 */
export function RestaurantForm({ onSubmit, isSubmitting, error }: RestaurantFormProps) {
  const methods = useForm<RestaurantFormValues>({
    resolver: zodResolver(restaurantFormSchema),
    defaultValues: EMPTY_RESTAURANT_FORM,
  });
  const { control, handleSubmit, formState, setError, clearErrors } = methods;

  // A restaurant that is never open could take no orders at all. This one rule
  // stays outside the schema because the day checkboxes have no single field to
  // hang it on, and Zod would have to report it against an arbitrary one.
  const submit: SubmitHandler<RestaurantFormValues> = (values) => {
    if (!WEEK.some((day) => values.openingHours[day].enabled)) {
      setError('root.openingHours', { message: 'Pick at least one day you are open.' });
      return;
    }
    clearErrors('root.openingHours');
    onSubmit(values);
  };

  return (
    <FormProvider {...methods}>
      <Paper
        component="form"
        variant="outlined"
        onSubmit={handleSubmit(submit)}
        noValidate
        sx={{ p: 3 }}
      >
        <Stack spacing={3}>
          <Section title="The basics">
            <Stack spacing={2}>
              <FormTextField
                control={control}
                name="name"
                label="Restaurant name"
                fullWidth
              />
  
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <FormTextField
                  control={control}
                  name="contactEmail"
                  label="Contact email"
                  type="email"
                  fullWidth
                />
                <FormTextField
                  control={control}
                  name="cuisineType"
                  label="Cuisine"
                  select
                  fullWidth
                >
                  {CUISINE_TYPES.map((cuisine) => (
                    <MenuItem key={cuisine} value={cuisine}>
                      {humanise(cuisine)}
                    </MenuItem>
                  ))}
                </FormTextField>
              </Stack>
  
              <FormTextField
                control={control}
                name="defaultPreparationTime"
                label="Default preparation time"
                type="number"
                helperText="Minutes, used to estimate delivery times"
                sx={{ maxWidth: 280 }}
              />
            </Stack>
          </Section>
  
          <Divider />
  
          <Section title="Where you are">
            <AddressFields />
          </Section>
  
          <Divider />
  
          <Section title="Pictures">
            <PictureUrlFields />
          </Section>
  
          <Divider />
  
          <Section title="Opening hours" subtitle="Tick the days you are open and set the hours.">
            <OpeningHoursFields />
            {formState.errors.root?.openingHours && (
              <Alert severity="error">{formState.errors.root.openingHours.message}</Alert>
            )}
          </Section>
  
          {error && <Alert severity="error">{error}</Alert>}
  
          <Box>
            <Button type="submit" variant="contained" size="large" disabled={isSubmitting}>
              {isSubmitting ? 'Creating' : 'Create restaurant'}
            </Button>
          </Box>
        </Stack>
      </Paper>
    </FormProvider>
  );
}

interface SectionProps {
  title: string;
  subtitle?: string;
  children: ReactNode;
}

function Section({ title, subtitle, children }: SectionProps) {
  return (
    <Stack spacing={1.5}>
      <Box>
        <Typography variant="h2" sx={{ fontSize: '1.15rem' }}>
          {title}
        </Typography>
        {subtitle && (
          <Typography variant="body2" color="text.secondary">
            {subtitle}
          </Typography>
        )}
      </Box>
      {children}
    </Stack>
  );
}
