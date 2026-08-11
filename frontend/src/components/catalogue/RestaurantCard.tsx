import { Box, Card, CardActionArea, CardContent, CardMedia, Chip, Stack, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

import type { RestaurantSummary } from '../../types/api';
import { formatPriceRange, humanise } from '../../utils/format';

export function RestaurantCard({ restaurant }: { restaurant: RestaurantSummary }) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardActionArea
        component={Link}
        to={`/restaurants/${restaurant.restaurantId}`}
        sx={{ height: '100%', display: 'block' }}
      >
        {restaurant.pictureUrls.length > 0 && (
          <CardMedia component="img" height="160" image={restaurant.pictureUrls[0]} alt="" />
        )}
        <CardContent>
          <Stack direction="row" spacing={1} sx={{ justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <Typography variant="h2" sx={{ fontSize: '1.15rem' }}>
              {restaurant.name}
            </Typography>
            <Chip
              size="small"
              label={restaurant.open ? 'Open' : 'Closed'}
              color={restaurant.open ? 'success' : 'default'}
            />
          </Stack>

          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {humanise(restaurant.cuisineType)} · {restaurant.address.city}
          </Typography>

          <Box sx={{ mt: 1.5 }}>
            <Typography variant="body2">
              {formatPriceRange(restaurant.lowestPrice, restaurant.highestPrice)}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {restaurant.menuSize} dishes · ~{restaurant.defaultPreparationTime} min prep
            </Typography>
          </Box>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
