import { Navigate } from 'react-router-dom';
import { Stack, Typography } from '@mui/material';

import { QueryState } from '../../components/QueryState';
import { RestaurantSummaryCard } from '../../components/owner/RestaurantSummaryCard';
import { useMyRestaurant } from '../../hooks/useMyRestaurant';

/**
 * The owner's management area (US-01).
 *
 * An owner with no restaurant yet is sent straight to the create form. Every owner has exactly one
 * restaurant (US-31), so there is nothing to choose between here.
 */
export function OwnerDashboardPage() {
  const { data: restaurant, isLoading, error } = useMyRestaurant();

  if (!isLoading && !error && restaurant === null) {
    return <Navigate to="/owner/restaurant/new" replace />;
  }

  return (
    <QueryState isLoading={isLoading} error={error}>
      {restaurant && (
        <Stack spacing={3}>
          <Typography variant="h1">My restaurant</Typography>
          <RestaurantSummaryCard restaurant={restaurant} />
        </Stack>
      )}
    </QueryState>
  );
}