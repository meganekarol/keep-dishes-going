import { Link, Navigate } from 'react-router-dom';
import { Button, Stack, Typography } from '@mui/material';

import { QueryState } from '../../components/shared/QueryState';
import { RestaurantStatusToggle } from '../../components/owner/RestaurantStatusToggle';
import { RestaurantSummaryCard } from '../../components/owner/RestaurantSummaryCard';
import { useMyRestaurant } from '../../hooks/useMyRestaurant';

/**
 * The owner's management area (US-01).
 *
 * An owner with no restaurant yet is sent straight to the create form. Every owner has exactly one
 * restaurant (US-31), so there is nothing to choose between here.
 */
export function OwnerDashboardPage() {
  const { data: restaurant, isLoading, isFetching, error } = useMyRestaurant();

  // isFetching, not isLoading: isLoading stays false once anything is cached, so
  // a background refetch would redirect on the stale answer. This covers the
  // first load too, which is always fetching.
  if (!isFetching && !error && restaurant === null) {
    return <Navigate to="/owner/restaurant/new" replace />;
  }

  return (
    <QueryState isLoading={isLoading} error={error}>
      {restaurant && (
        <Stack spacing={3}>
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={2}
            sx={{ alignItems: { sm: 'center' }, justifyContent: 'space-between' }}
          >
            <Typography variant="h1">My restaurant</Typography>
            <Stack direction="row" spacing={2}>
              <Button variant="contained" component={Link} to="/owner/orders">
                Orders
              </Button>
              <Button variant="contained" component={Link} to="/owner/menu">
                Manage my menu
              </Button>
            </Stack>
          </Stack>
          <RestaurantStatusToggle
            restaurantId={restaurant.restaurantId}
            status={restaurant.status}
          />
          <RestaurantSummaryCard restaurant={restaurant} />
        </Stack>
      )}
    </QueryState>
  );
}