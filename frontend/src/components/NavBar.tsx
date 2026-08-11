import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import { Link } from 'react-router-dom';
import { useAuth } from 'react-oidc-context';

export function NavBar() {
  const auth = useAuth();

  return (
    <AppBar position="static" color="primary" elevation={0}>
      <Toolbar>
        <RestaurantMenuIcon sx={{ mr: 1.5 }} />
        <Typography
          variant="h6"
          component={Link}
          to="/restaurants"
          sx={{ color: 'inherit', textDecoration: 'none', fontWeight: 700 }}
        >
          Keep Dishes Going
        </Typography>

        <Box sx={{ flexGrow: 1 }} />

        {auth.isAuthenticated ? (
          <>
            <Typography variant="body2" sx={{ mr: 2 }}>
              {auth.user?.profile.preferred_username ?? auth.user?.profile.email}
            </Typography>
            <Button color="inherit" onClick={() => void auth.signoutRedirect()}>
              Log out
            </Button>
          </>
        ) : (
          // Customers never need this ordering and tracking are open (US-30).
          // It is here for restaurant owners.
          <Button color="inherit" onClick={() => void auth.signinRedirect()}>
            Owner log in
          </Button>
        )}
      </Toolbar>
    </AppBar>
  );
}
