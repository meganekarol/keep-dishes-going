import { useContext } from 'react';
import { AppBar, Badge, Box, Button, IconButton, Toolbar, Tooltip, Typography } from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import ShoppingBasketIcon from '@mui/icons-material/ShoppingBasket';
import { Link } from 'react-router-dom';

import BasketContext from '../../context/BasketContext';
import SecurityContext from '../../context/SecurityContext';

export function NavBar() {
  const { isAuthenticated, loggedInUser, login, logout } = useContext(SecurityContext);
  const { itemCount } = useContext(BasketContext);

  return (
    <AppBar position="static" color="primary" elevation={0}>
      <Toolbar>
        <RestaurantMenuIcon sx={{ mr: 1.5 }} />
        <Typography
          variant="h6"
          component={Link}
          to="/"
          sx={{ color: 'inherit', textDecoration: 'none', fontWeight: 700 }}
        >
          Keep Dishes Going
        </Typography>

        <Box sx={{ flexGrow: 1 }} />

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
            <Button color="inherit" component={Link} to="/owner">
              My restaurant
            </Button>
            <Typography variant="body2" sx={{ mx: 2 }}>
              {loggedInUser?.name}
            </Typography>
            <Button color="inherit" onClick={logout}>
              Log out
            </Button>
          </>
        ) : (
          // Customers never need this: ordering and tracking are open (US-30).
          // It is here for restaurant owners.
          <Button color="inherit" onClick={login}>
            Owner log in
          </Button>
        )}
      </Toolbar>
    </AppBar>
  );
}