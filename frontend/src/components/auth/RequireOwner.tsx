import { type PropsWithChildren, useContext, useEffect } from 'react';
import { Alert, Box, CircularProgress, Typography } from '@mui/material';

import SecurityContext from '../../context/SecurityContext';

/**
 * Gates the owner's management area behind a Keycloak sign-in (US-01).
 *
 * It decides what to show, not what is allowed. The backend re-checks the token
 * and the owner role on every request, which is what actually protects the data.
 */
export function RequireOwner({ children }: PropsWithChildren) {
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

  if (!loggedInUser?.roles.includes('owner')) {
    return <Alert severity="error">This area is for restaurant owners.</Alert>;
  }

  return <>{children}</>;
}