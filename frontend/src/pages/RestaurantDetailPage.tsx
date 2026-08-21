import { useContext, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  MenuItem as SelectItem,
  Snackbar,
  Stack,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';

import AccessTimeIcon from '@mui/icons-material/AccessTime';

import { MenuItemCard } from '../components/catalogue/MenuItemCard';
import { CoverImage } from '../components/shared/CoverImage';
import { OpeningHoursTable } from '../components/shared/OpeningHoursTable';
import { QueryState } from '../components/shared/QueryState';
import BasketContext from '../context/BasketContext';
import { useCustomerLocation } from '../hooks/useCustomerLocation';
import { useMenu, useRestaurant } from '../hooks/useRestaurants';
import type { DishSortOrder, DishType, FoodTag, MenuItem } from '../types/api';
import { humanise } from '../utils/format';

const DISH_TYPES: DishType[] = ['STARTER', 'MAIN', 'DESSERT'];
const FOOD_TAGS: FoodTag[] = ['GLUTEN_FREE', 'LACTOSE_FREE', 'VEGAN', 'SPICY'];

const SORT_LABELS: Record<DishSortOrder, string> = {
  NAME: 'Name',
  PRICE_ASC: 'Price, low to high',
  PRICE_DESC: 'Price, high to low',
};

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
  const { denied: locationDenied } = useCustomerLocation();

  const { basket, addItem } = useContext(BasketContext);
  const [dishFromElsewhere, setDishFromElsewhere] = useState<MenuItem | null>(null);
  const [justAdded, setJustAdded] = useState('');

  const basketIsForThisRestaurant = basket?.restaurantId === restaurantId;

  function putInBasket(item: MenuItem) {
    addItem(
      { restaurantId, restaurantName: restaurantQuery.data?.name ?? '' },
      { dishId: item.dishId, name: item.name, unitPrice: item.price, quantity: 1 },
    );
    setJustAdded(item.name);
  }

  function handleAdd(item: MenuItem) {
    // One order goes to one kitchen, so this dish and a basket from another
    // restaurant cannot travel together. Ask before emptying that basket.
    if (basket && !basketIsForThisRestaurant) {
      setDishFromElsewhere(item);
      return;
    }
    putInBasket(item);
  }

  function quantityInBasket(dishId: string) {
    if (!basketIsForThisRestaurant) return 0;
    return basket?.lines.find((line) => line.dishId === dishId)?.quantity ?? 0;
  }

  return (
    <QueryState isLoading={restaurantQuery.isLoading} error={restaurantQuery.error}>
      {restaurantQuery.data && (
        <>
          <Card sx={{ mb: 4 }}>
            <CoverImage
              src={restaurantQuery.data.pictureUrls[0]}
              alt={restaurantQuery.data.name}
              ratio={24 / 7}
            />
            <CardContent sx={{ p: { xs: 2.5, md: 3 } }}>
              <Stack direction="row" spacing={2} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
                <Typography variant="h1">{restaurantQuery.data.name}</Typography>
                <Chip
                  label={restaurantQuery.data.open ? 'Open' : 'Closed'}
                  color={restaurantQuery.data.open ? 'success' : 'default'}
                />
              </Stack>

              <Typography color="text.secondary" sx={{ mt: 0.5 }}>
                {humanise(restaurantQuery.data.cuisineType)} · {restaurantQuery.data.address.street}{' '}
                {restaurantQuery.data.address.number}, {restaurantQuery.data.address.postalCode}{' '}
                {restaurantQuery.data.address.city}
              </Typography>

              {/* Only worth saying while they can act on it: a closed kitchen
                  delivers in no minutes at all. */}
              {restaurantQuery.data.open && (
                <Chip
                  size="small"
                  variant="outlined"
                  icon={<AccessTimeIcon />}
                  label={`Roughly ${restaurantQuery.data.guesstimatedDeliveryTime} minutes to your door${
                    locationDenied ? ', assuming an average distance' : ''
                  }`}
                  sx={{ mt: 2 }}
                />
              )}

              <Box sx={{ mt: 2.5 }}>
                <OpeningHoursTable hours={restaurantQuery.data.openingHours} />
              </Box>
            </CardContent>
          </Card>

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
                {menuQuery.data?.map((item) => (
                  <MenuItemCard
                    key={item.dishId}
                    item={item}
                    onAdd={handleAdd}
                    inBasket={quantityInBasket(item.dishId)}
                  />
                ))}
              </Box>
            )}
          </QueryState>

          <Dialog open={dishFromElsewhere !== null} onClose={() => setDishFromElsewhere(null)}>
            <DialogTitle>Start a new basket?</DialogTitle>
            <DialogContent>
              <DialogContentText>
                Your basket still holds dishes from {basket?.restaurantName}. Adding{' '}
                {dishFromElsewhere?.name} empties it and starts over at{' '}
                {restaurantQuery.data.name}.
              </DialogContentText>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setDishFromElsewhere(null)}>Keep my basket</Button>
              <Button
                variant="contained"
                onClick={() => {
                  if (dishFromElsewhere) putInBasket(dishFromElsewhere);
                  setDishFromElsewhere(null);
                }}
              >
                Start a new basket
              </Button>
            </DialogActions>
          </Dialog>

          <Snackbar
            open={justAdded !== ''}
            autoHideDuration={3000}
            onClose={() => setJustAdded('')}
            message={`${justAdded} added to your basket`}
          />
        </>
      )}
    </QueryState>
  );
}
