import { Navigate, useNavigate } from 'react-router-dom';
import { Stack, Typography } from '@mui/material';
import type { SubmitHandler } from 'react-hook-form';

import { DishForm, EMPTY_DISH_FORM, toDishRequest } from '../../components/owner/DishForm';
import type { DishFormValues } from '../../components/owner/DishForm';
import { QueryState } from '../../components/shared/QueryState';
import { useCreateDish } from '../../hooks/useDishDrafts';
import { useMyRestaurant } from '../../hooks/useMyRestaurant';

/**
 * Adding a dish (US-03).
 *
 * A new dish starts as a draft, so nothing reaches customers until it is
 * published.
 */
export function NewDishPage() {
  const navigate = useNavigate();
  const { data: restaurant, isLoading, error } = useMyRestaurant();
  const { mutate, isPending, error: saveError } = useCreateDish(restaurant?.restaurantId ?? '');

  if (!isLoading && !error && restaurant === null) {
    return <Navigate to="/owner/restaurant/new" replace />;
  }

  const submit: SubmitHandler<DishFormValues> = (values) => {
    mutate(toDishRequest(values), {
      // replace, so the back button does not return to a form that has already
      // been submitted.
      onSuccess: () => navigate('/owner/menu', { replace: true }),
    });
  };

  return (
    <QueryState isLoading={isLoading} error={error}>
      {restaurant && (
        <Stack spacing={3}>
          <div>
            <Typography variant="h1" gutterBottom>
              Add a dish
            </Typography>
            <Typography color="text.secondary">
              It is saved as a draft. Put it on the menu from your menu page when it is ready.
            </Typography>
          </div>

          <DishForm
            defaultValues={EMPTY_DISH_FORM}
            onSubmit={submit}
            isSubmitting={isPending}
            submitLabel="Save as draft"
            error={saveError instanceof Error ? saveError.message : undefined}
          />
        </Stack>
      )}
    </QueryState>
  );
}