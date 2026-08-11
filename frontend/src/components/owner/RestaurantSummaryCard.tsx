import { Box, Card, CardContent, CardMedia, Chip, Stack, Typography } from '@mui/material';

import { OpeningHoursTable } from '../OpeningHoursTable';
import type { OwnerRestaurant, RestaurantStatus } from '../../types/api';
import { humanise } from '../../utils/format';

const STATUS_COLOURS: Record<RestaurantStatus, 'success' | 'default' | 'info' | 'warning'> = {
  OPEN: 'success',
  CLOSED: 'default',
  MANUALLY_OPENED: 'info',
  MANUALLY_CLOSED: 'warning',
};

interface RestaurantSummaryCardProps {
  restaurant: OwnerRestaurant;
}

/**
 * What the owner sees of their own restaurant.
 *
 * This shows the stored status rather than a computed open flag, because the
 * owner needs to know whether they are closed by the schedule or because they
 * closed the doors by hand.
 */
export function RestaurantSummaryCard({ restaurant }: RestaurantSummaryCardProps) {
  return (
    <Card variant="outlined">
      {restaurant.pictureUrls.length > 0 && (
        <CardMedia component="img" height="200" image={restaurant.pictureUrls[0]} alt="" />
      )}
      <CardContent>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center', mb: 1 }}>
          <Typography variant="h2">{restaurant.name}</Typography>
          <Chip label={humanise(restaurant.status)} color={STATUS_COLOURS[restaurant.status]} />
        </Stack>

        <Stack direction={{ xs: 'column', md: 'row' }} spacing={3} sx={{ mt: 2 }}>
          <Stack spacing={0.5} sx={{ flex: 1 }}>
            <Detail label="Cuisine" value={humanise(restaurant.cuisineType)} />
            <Detail
              label="Address"
              value={`${restaurant.address.street} ${restaurant.address.number}, ${restaurant.address.postalCode} ${restaurant.address.city}, ${restaurant.address.country}`}
            />
            <Detail label="Contact" value={restaurant.contactEmail} />
            <Detail label="Preparation time" value={`${restaurant.defaultPreparationTime} minutes`} />
            <Detail label="Pictures" value={`${restaurant.pictureUrls.length}`} />
          </Stack>

          <Box>
            <OpeningHoursTable hours={restaurant.openingHours} />
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}

interface DetailProps {
  label: string;
  value: string;
}

function Detail({ label, value }: DetailProps) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body2">{value}</Typography>
    </Box>
  );
}