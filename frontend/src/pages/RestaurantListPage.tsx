import { useState } from 'react';
import { Alert, Box, Stack, ToggleButton, ToggleButtonGroup, Typography } from '@mui/material';
import MapOutlinedIcon from '@mui/icons-material/MapOutlined';
import ViewModuleOutlinedIcon from '@mui/icons-material/ViewModuleOutlined';

import { QueryState } from '../components/shared/QueryState';
import { RestaurantCard } from '../components/catalogue/RestaurantCard';
import { RestaurantFilterForm } from '../components/catalogue/RestaurantFilterForm';
import { RestaurantMap } from '../components/catalogue/RestaurantMap';
import { useRestaurants } from '../hooks/useRestaurants';
import type { RestaurantFilters } from '../services/restaurantService';

type View = 'list' | 'map';

/**
 * The catalogue (US-14, US-16).
 *
 * Filtering happens server-side: the price filter means "at least one dish in
 * this range", which the client cannot work out from a summary alone.
 */
export function RestaurantListPage() {
  const [filters, setFilters] = useState<RestaurantFilters>({});
  const [view, setView] = useState<View>('list');
  const { data, isLoading, error } = useRestaurants(filters);

  return (
    <>
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        sx={{ mb: 3, alignItems: { sm: 'flex-end' }, justifyContent: 'space-between' }}
      >
        <Box>
          <Typography variant="h1" gutterBottom>
            Restaurants
          </Typography>
          <Typography color="text.secondary">
            {data
              ? `${data.length} ${data.length === 1 ? 'kitchen is' : 'kitchens are'} taking orders around you.`
              : 'Finding kitchens around you.'}
          </Typography>
        </Box>

        {/* Two ways into the same filtered set, not two pages (US-14). */}
        <ToggleButtonGroup
          exclusive
          size="small"
          value={view}
          onChange={(_event, next: View | null) => next && setView(next)}
        >
          <ToggleButton value="list">
            <ViewModuleOutlinedIcon fontSize="small" sx={{ mr: 0.5 }} />
            List
          </ToggleButton>
          <ToggleButton value="map">
            <MapOutlinedIcon fontSize="small" sx={{ mr: 0.5 }} />
            Map
          </ToggleButton>
        </ToggleButtonGroup>
      </Stack>

      <RestaurantFilterForm onApply={setFilters} />

      <QueryState isLoading={isLoading} error={error}>
        {data?.length === 0 ? (
          <Alert severity="info">No restaurants match these filters.</Alert>
        ) : view === 'map' ? (
          <RestaurantMap restaurants={data ?? []} />
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