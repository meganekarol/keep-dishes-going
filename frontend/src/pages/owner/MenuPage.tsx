import { Link, Navigate } from 'react-router-dom';
import { Alert, Button, Stack, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';

import { MenuSummaryBar } from '../../components/owner/MenuSummaryBar';
import { OwnerDishCard } from '../../components/owner/OwnerDishCard';
import { QueryState } from '../../components/shared/QueryState';
import { useMyRestaurant } from '../../hooks/useMyRestaurant';
import { useOwnerMenu } from '../../hooks/useOwnerMenu';

/**
 * The owner's menu (US-03).
 *
 * The dishes hang off the restaurant, so the restaurant is loaded first and the
 * menu query waits on its id. An owner who has not created a restaurant yet is
 * sent to that form instead, the same as on the dashboard.
 */
export function MenuPage() {
  const { data: restaurant, isLoading, error } = useMyRestaurant();
  const menuQuery = useOwnerMenu(restaurant?.restaurantId ?? '');

  if (!isLoading && !error && restaurant === null) {
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
            <Typography variant="h1">My menu</Typography>
            {/* Always available, however full the menu is: the cap is on
                publishing a dish, not on writing one down. */}
            <Button variant="contained" startIcon={<AddIcon />} component={Link} to="/owner/menu/new">
              Add a dish
            </Button>
          </Stack>

          <QueryState isLoading={menuQuery.isLoading} error={menuQuery.error}>
            {menuQuery.data && (
              <Stack spacing={3}>
                <MenuSummaryBar menu={menuQuery.data} />

                {menuQuery.data.dishes.length === 0 ? (
                  <Alert severity="info">
                    You have no dishes yet. Add one to start building your menu.
                  </Alert>
                ) : (
                  <Stack spacing={2}>
                    {menuQuery.data.dishes.map((dish) => (
                      <OwnerDishCard key={dish.dishId} dish={dish} />
                    ))}
                  </Stack>
                )}
              </Stack>
            )}
          </QueryState>
        </Stack>
      )}
    </QueryState>
  );
}