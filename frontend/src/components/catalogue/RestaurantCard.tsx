import { Box, Card, CardActionArea, CardContent, Chip, Stack, Typography } from '@mui/material';
import { Link } from 'react-router-dom';

import { CoverImage } from '../shared/CoverImage';
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
        {/* Always rendered, even with no picture: a card that skipped the image
            would sit shorter than its neighbours and break the grid's rhythm. */}
        <Box sx={{ position: 'relative' }}>
          <CoverImage src={restaurant.pictureUrls[0]} alt={restaurant.name} ratio={16 / 10} />
          <Chip
            size="small"
            label={restaurant.open ? 'Open' : 'Closed'}
            color={restaurant.open ? 'success' : 'default'}
            sx={{ position: 'absolute', top: 12, left: 12, boxShadow: 1 }}
          />
        </Box>
        <CardContent>
          <Typography variant="h3" sx={{ mb: 0.25 }}>
            {restaurant.name}
          </Typography>

          <Typography variant="body2" color="text.secondary">
            {humanise(restaurant.cuisineType)} · {restaurant.address.city}
          </Typography>

          <Stack
            direction="row"
            spacing={1}
            sx={{ mt: 1.5, alignItems: 'center', justifyContent: 'space-between' }}
          >
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {formatPriceRange(restaurant.lowestPrice, restaurant.highestPrice)}
            </Typography>
            {/* The delivery guess, not the prep time: what a customer compares
                restaurants on is when the food turns up. */}
            <Chip
              size="small"
              variant="outlined"
              label={`~${restaurant.guesstimatedDeliveryTime} min`}
            />
          </Stack>

          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
            {restaurant.menuSize} dishes on the menu
          </Typography>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
