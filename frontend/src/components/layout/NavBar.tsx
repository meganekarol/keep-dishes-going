import { useContext } from 'react';
import { AppBar, Badge, Box, Button, IconButton, Toolbar, Tooltip, Typography } from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import ShoppingBasketIcon from '@mui/icons-material/ShoppingBasket';
import { Link } from 'react-router-dom';

import BasketContext from '../../context/BasketContext';
import SecurityContext from '../../context/SecurityContext';
import { usePlacedOrders } from '../../hooks/usePlacedOrders';

export function NavBar() {
  const { isAuthenticated, loggedInUser, login, logout } = useContext(SecurityContext);
  const { itemCount } = useContext(BasketContext);
  const { orders } = usePlacedOrders();

  return (
    <AppBar position="sticky">
      <Toolbar>
        <RestaurantMenuIcon sx={{ mr: 1.5, color: 'primary.main' }} />
        <Typography
          variant="h6"
          component={Link}
          to="/"
          sx={{ color: 'text.primary', textDecoration: 'none' }}
        >
          Keep Dishes Going
        </Typography>

        <Button color="inherit" component={Link} to="/restaurants" sx={{ ml: 3 }}>
          Restaurants
        </Button>

        <Box sx={{ flexGrow: 1 }} />

        {/* The only way back to a guest's order once the tab is closed, so it
            stays visible for as long as this browser remembers one. */}
        {orders.length > 0 && (
          <Button color="inherit" component={Link} to="/orders">
            My orders
          </Button>
        )}

        {/* Hidden while empty: an owner working on their menu has no basket,
            and an empty one is nothing to click. */}
        {itemCount > 0 && (
          <Tooltip title="Your basket">
            <IconButton color="inherit" component={Link} to="/basket" sx={{ mr: 1 }}>
              <Badge badgeContent={itemCount} color="secondary">
                <ShoppingBasketIcon />
              </Badge>
            </IconButton>
          </Tooltip>
        )}

        {isAuthenticated() ? (
          <>
            {loggedInUser?.roles.includes('admin') && (
              <Button variant="outlined" color="inherit" component={Link} to="/admin/price-ranges">
                Price range criteria
              </Button>
            )}
            {loggedInUser?.roles.includes('owner') && (
              <Button variant="outlined" color="inherit" component={Link} to="/owner">
                My restaurant
              </Button>
            )}
            <Typography variant="body2" color="text.secondary" sx={{ mx: 2 }}>
              {loggedInUser?.name}
            </Typography>
            <Button color="inherit" onClick={logout}>
              Log out
            </Button>
          </>
        ) : (
          // Customers never need this: ordering and tracking are open (US-30).
          // It is here for restaurant owners.
          <Button variant="contained" onClick={login}>
            Owner log in
          </Button>
        )}
      </Toolbar>
    </AppBar>
  );
}