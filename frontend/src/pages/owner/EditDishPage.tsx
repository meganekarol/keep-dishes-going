import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { Alert, Stack, Typography } from '@mui/material';
import type { SubmitHandler } from 'react-hook-form';

import { DishForm, toDishFormValues, toDishRequest } from '../../components/owner/DishForm';
import type { DishFormValues } from '../../components/owner/DishForm';
import { QueryState } from '../../components/shared/QueryState';
import { useEditDishDraft } from '../../hooks/useDishDrafts';
import { useMyRestaurant } from '../../hooks/useMyRestaurant';
import { useOwnerMenu } from '../../hooks/useOwnerMenu';

/**
 * Editing a dish (US-03).
 *
 * There is no endpoint for a single dish, so the dish is picked out of the
 * owner's menu, which the menu page has usually already cached.
 */
export function EditDishPage() {
  const { dishId } = useParams<{ dishId: string }>();
  const navigate = useNavigate();

  const { data: restaurant, isLoading, error } = useMyRestaurant();
  const restaurantId = restaurant?.restaurantId ?? '';
  const menuQuery = useOwnerMenu(restaurantId);
  const { mutate, isPending, error: saveError } = useEditDishDraft(restaurantId);

  if (!isLoading && !error && restaurant === null) {
    return <Navigate to="/owner/restaurant/new" replace />;
  }

  const dish = menuQuery.data?.dishes.find((candidate) => candidate.dishId === dishId);

  // The draft first: it is the most recent thing the owner typed, and editing
  // should carry on from there rather than throw those changes away.
  const version = dish?.draft ?? dish?.published ?? null;

  const submit: SubmitHandler<DishFormValues> = (values) => {
    if (!dishId) return;

    mutate(
      { dishId, request: toDishRequest(values) },
      { onSuccess: () => navigate('/owner/menu', { replace: true }) },
    );
  };

  return (
    <QueryState isLoading={isLoading} error={error}>
      <QueryState isLoading={menuQuery.isLoading} error={menuQuery.error}>
        {menuQuery.data && !version ? (
          <Alert severity="error">That dish is not on your menu.</Alert>
        ) : (
          version && (
            <Stack spacing={3}>
              <div>
                <Typography variant="h1" gutterBottom>
                  Edit {version.name}
                </Typography>
                <Typography color="text.secondary">
                  {dish?.published
                    ? 'Your changes are saved as a draft. Customers keep ordering the version that is live until you publish it.'
                    : 'This dish is still a draft, so nobody sees these changes yet.'}
                </Typography>
              </div>

              <DishForm
                defaultValues={toDishFormValues(version)}
                onSubmit={submit}
                isSubmitting={isPending}
                submitLabel="Save changes"
                error={saveError instanceof Error ? saveError.message : undefined}
              />
            </Stack>
          )
        )}
      </QueryState>
    </QueryState>
  );
}