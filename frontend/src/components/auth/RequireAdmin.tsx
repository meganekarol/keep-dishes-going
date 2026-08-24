import { type PropsWithChildren, useContext, useEffect } from 'react';
import { Alert, Box, CircularProgress, Typography } from '@mui/material';

import SecurityContext from '../../context/SecurityContext';

/**
 * Gates the platform admin's area behind a Keycloak sign-in (US-32).
 *
 * It decides what to show, not what is allowed. The backend re-checks the token
 * and the admin role on every request, which is what actually protects the data.
 */
export function RequireAdmin({ children }: PropsWithChildren) {
  const { isInitialised, isAuthenticated, loggedInUser, login } = useContext(SecurityContext);

  useEffect(() => {
    if (isInitialised && !isAuthenticated()) {
      login();
    }
  }, [isInitialised, isAuthenticated, login]);

  if (!isAuthenticated()) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, py: 8 }}>
        <CircularProgress />
        <Typography color="text.secondary">Signing you in</Typography>
      </Box>
    );
  }

  // Signed in but not an admin. Sending them back to the login screen would
  // just sign them in again, so say plainly that this is not for them.
  if (!loggedInUser?.roles.includes('admin')) {
    return <Alert severity="error">This area is for the platform administrator.</Alert>;
  }

  return <>{children}</>;
}