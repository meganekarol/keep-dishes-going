import { useContext } from 'react';
import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import { Link } from 'react-router-dom';

import SecurityContext from '../../context/SecurityContext';

export function NavBar() {
  const { isAuthenticated, loggedInUser, login, logout } = useContext(SecurityContext);

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