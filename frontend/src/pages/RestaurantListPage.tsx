import { useState } from 'react';
import { Alert, Box, Typography } from '@mui/material';

import { QueryState } from '../components/shared/QueryState';
import { RestaurantCard } from '../components/catalogue/RestaurantCard';
import { RestaurantFilterForm } from '../components/catalogue/RestaurantFilterForm';
import { useRestaurants } from '../hooks/useRestaurants';
import type { RestaurantFilters } from '../services/restaurantService';

/**
 * The catalogue (US-14, US-16).
 *
 * Filtering happens server-side: the price filter means "at least one dish in
 * this range", which the client cannot work out from a summary alone.
 */
export function RestaurantListPage() {
  const [filters, setFilters] = useState<RestaurantFilters>({});
  const { data, isLoading, error } = useRestaurants(filters);

  return (
    <>
      <Box sx={{ mb: 3 }}>
        <Typography variant="h1" gutterBottom>
          Restaurants
        </Typography>
        <Typography color="text.secondary">
          {data
            ? `${data.length} ${data.length === 1 ? 'kitchen is' : 'kitchens are'} taking orders around you.`
            : 'Finding kitchens around you.'}
        </Typography>
      </Box>

      <RestaurantFilterForm onApply={setFilters} />

      <QueryState isLoading={isLoading} error={error}>
        {data?.length === 0 ? (
          <Alert severity="info">No restaurants match these filters.</Alert>
        ) : (
          <Box
            sx={{
              display: 'grid',
              gap: 3,
              gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' },
            }}
          >
            {data?.map((restaurant) => (
              <RestaurantCard key={restaurant.restaurantId} restaurant={restaurant} />
            ))}
          </Box>
        )}
      </QueryState>
    </>
  );
}
