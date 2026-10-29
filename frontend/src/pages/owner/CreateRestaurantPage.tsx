import { useNavigate } from 'react-router-dom';
import { Stack, Typography } from '@mui/material';
import type { SubmitHandler } from 'react-hook-form';

import { RestaurantForm, WEEK } from '../../components/owner/RestaurantForm';
import type { RestaurantFormValues } from '../../components/owner/RestaurantForm';
import { useCreateRestaurant } from '../../hooks/useCreateRestaurant';
import type { CreateRestaurantRequest, CuisineType, WeeklyOpeningHours } from '../../types/api';

/**
 * Turns what the owner typed into what the API expects.
 *
 * The form keeps numbers as text and every weekday as a row, because that is
 * what inputs produce. The backend wants real numbers and only the days the
 * restaurant is actually open.
 */
function toCreateRestaurantRequest(values: RestaurantFormValues): CreateRestaurantRequest {
  const openingHours: WeeklyOpeningHours = {};
  for (const day of WEEK) {
    const dayValues = values.openingHours[day];
    if (dayValues.enabled) {
      openingHours[day] = { open: dayValues.open, closed: dayValues.closed };
    }
  }

  return {
    name: values.name.trim(),
    address: {
      street: values.address.street.trim(),
      number: Number(values.address.number),
      postalCode: values.address.postalCode.trim(),
      city: values.address.city.trim(),
      country: values.address.country.trim(),
    },
    contactEmail: values.contactEmail.trim(),
    // Never empty by the time we get here: the form requires a choice.
    cuisineType: values.cuisineType as CuisineType,
    defaultPreparationTime: Number(values.defaultPreparationTime),
    openingHours,
    pictureUrls: values.pictureUrls.map((picture) => picture.url.trim()).filter(Boolean),
  };
}

/**
 * The form an owner meets on their first sign-in, when no restaurant is linked
 * to them yet (US-02).
 */
export function CreateRestaurantPage() {
  const navigate = useNavigate();
  const { mutate, isPending, error } = useCreateRestaurant();

  const submit: SubmitHandler<RestaurantFormValues> = (values) => {
    mutate(toCreateRestaurantRequest(values), {
      // replace, so the back button does not return to a form that has already
      // been submitted.
      onSuccess: () => navigate('/owner', { replace: true }),
    });
  };

  return (
    <Stack spacing={3}>
      <div>
        <Typography variant="h1" gutterBottom>
          Set up your restaurant
        </Typography>
        <Typography color="text.secondary">
          Tell customers who you are and when you are open. You can add dishes once this is done.
        </Typography>
      </div>

      <RestaurantForm
        onSubmit={submit}
        isSubmitting={isPending}
        error={error instanceof Error ? error.message : undefined}
      />
    </Stack>
  );
}