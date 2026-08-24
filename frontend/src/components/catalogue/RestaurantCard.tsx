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

        <Box sx={{ position: 'relative' }}>

          <CoverImage
            src={restaurant.pictureUrls[0]}
            alt={restaurant.name}
            ratio={16 / 10}
            scrim
            placeholderTone="dark"
          />
          <Chip
            size="small"
            label={restaurant.open ? 'Open' : 'Closed'}
            color={restaurant.open ? 'success' : 'warning'}
            sx={{ position: 'absolute', top: 12, left: 12, boxShadow: 1 }}
          />
          <Typography
            variant="h3"
            sx={{
              position: 'absolute',
              left: 16,
              right: 16,
              bottom: 12,
              color: 'common.white',
              textShadow: '0 1px 8px rgba(28,25,23,.5)',
            }}
          >
            {restaurant.name}
          </Typography>
        </Box>
        <CardContent>
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
