import { useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Chip,
  Divider,
  MenuItem as SelectItem,
  Paper,
  Stack,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';

import { MenuItemCard } from '../components/MenuItemCard';
import { QueryState } from '../components/QueryState';
import { useMenu, useRestaurant } from '../hooks/useRestaurants';
import type { DayOfWeek, DishSortOrder, DishType, FoodTag, RestaurantDetail } from '../types/api';
import { formatTime, humanise } from '../utils/format';

const DISH_TYPES: DishType[] = ['STARTER', 'MAIN', 'DESSERT'];
const FOOD_TAGS: FoodTag[] = ['GLUTEN_FREE', 'LACTOSE_FREE', 'VEGAN', 'SPICY'];

const SORT_LABELS: Record<DishSortOrder, string> = {
  NAME: 'Name',
  PRICE_ASC: 'Price, low to high',
  PRICE_DESC: 'Price, high to low',
};

const WEEK: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

/**
 * One restaurant and its menu (US-15), with the dish filters and sort of
 * US-17/US-18.
 *
 * The header comes from the detail endpoint and the dish list from the dishes
 * endpoint, so changing a filter refetches the menu without reloading the
 * restaurant around it.
 */
export function RestaurantDetailPage() {
  const { restaurantId = '' } = useParams();
  const [type, setType] = useState<DishType | ''>('');
  const [foodTags, setFoodTags] = useState<FoodTag[]>([]);
  const [sort, setSort] = useState<DishSortOrder>('NAME');

  const restaurantQuery = useRestaurant(restaurantId);
  const menuQuery = useMenu(restaurantId, { type, foodTags, sort });

  return (
    <QueryState isLoading={restaurantQuery.isLoading} error={restaurantQuery.error}>
      {restaurantQuery.data && (
        <>
          <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
            <Typography variant="h1">{restaurantQuery.data.name}</Typography>
            <Chip
              label={restaurantQuery.data.open ? 'Open' : 'Closed'}
              color={restaurantQuery.data.open ? 'success' : 'default'}
            />
          </Stack>

          <Typography color="text.secondary" gutterBottom>
            {humanise(restaurantQuery.data.cuisineType)} · {restaurantQuery.data.address.street}{' '}
            {restaurantQuery.data.address.number}, {restaurantQuery.data.address.postalCode}{' '}
            {restaurantQuery.data.address.city}
          </Typography>

          <OpeningHours hours={restaurantQuery.data.openingHours} />

          <Divider sx={{ my: 3 }} />

          <Typography variant="h2" gutterBottom>
            Menu
          </Typography>

          <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} sx={{ mb: 3, alignItems: { md: 'center' } }}>
            <TextField
              select
              size="small"
              label="Course"
              value={type}
              onChange={(event) => setType(event.target.value as DishType | '')}
              sx={{ minWidth: 160 }}
            >
              <SelectItem value="">All courses</SelectItem>
              {DISH_TYPES.map((dishType) => (
                <SelectItem key={dishType} value={dishType}>
                  {humanise(dishType)}
                </SelectItem>
              ))}
            </TextField>

            <TextField
              select
              size="small"
              label="Sort by"
              value={sort}
              onChange={(event) => setSort(event.target.value as DishSortOrder)}
              sx={{ minWidth: 200 }}
            >
              {(Object.keys(SORT_LABELS) as DishSortOrder[]).map((order) => (
                <SelectItem key={order} value={order}>
                  {SORT_LABELS[order]}
                </SelectItem>
              ))}
            </TextField>

            {/* Tags narrow the menu together, not separately: picking vegan and
                gluten-free asks for dishes that are both. */}
            <ToggleButtonGroup
              size="small"
              value={foodTags}
              onChange={(_event, next: FoodTag[]) => setFoodTags(next)}
            >
              {FOOD_TAGS.map((tag) => (
                <ToggleButton key={tag} value={tag}>
                  {humanise(tag)}
                </ToggleButton>
              ))}
            </ToggleButtonGroup>
          </Stack>

          <QueryState isLoading={menuQuery.isLoading} error={menuQuery.error}>
            {menuQuery.data?.length === 0 ? (
              <Alert severity="info">No dishes match these filters.</Alert>
            ) : (
              <Box
                sx={{
                  display: 'grid',
                  gap: 2,
                  gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' },
                }}
              >
                {menuQuery.data?.map((item) => <MenuItemCard key={item.dishId} item={item} />)}
              </Box>
            )}
          </QueryState>
        </>
      )}
    </QueryState>
  );
}

interface openingHoursProps {
  hours: RestaurantDetail['openingHours']
}

/** A closed restaurant is only useful if the customer can see when to come back. */
function OpeningHours({ hours }: openingHoursProps) {
  return (
    <Paper variant="outlined" elevation={0} sx={{ p: 2, mt: 2, maxWidth: 360 }}>
      <Typography variant="subtitle2" gutterBottom>
        Opening hours
      </Typography>
      <Stack spacing={0.5}>
        {WEEK.map((day) => {
          const range = hours[day];
          return (
            <Stack key={day} direction="row" sx={{ justifyContent: 'space-between' }}>
              <Typography variant="body2">{humanise(day)}</Typography>
              <Typography variant="body2" color="text.secondary">
                {range ? `${formatTime(range.open)} – ${formatTime(range.closed)}` : 'Closed'}
              </Typography>
            </Stack>
          );
        })}
      </Stack>
    </Paper>
  );
}
