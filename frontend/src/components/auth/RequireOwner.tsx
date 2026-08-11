import type { ReactNode } from 'react';
import { Alert, Box, CircularProgress, Typography } from '@mui/material';
import { useAutoSignin } from 'react-oidc-context';

export function RequireOwner({ children }: { children: ReactNode }) {
  const { isLoading, isAuthenticated, error } = useAutoSignin({
    signinArgs: { redirect_uri: window.location.origin + window.location.pathname },
  });

  if (error) {
    return (
      <Alert severity="error">
        Signing in failed: {error.message}
      </Alert>
    );
  }

  if (isLoading || !isAuthenticated) {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, py: 8 }}>
        <CircularProgress />
        <Typography color="text.secondary">Taking you to the sign-in page…</Typography>
      </Box>
    );
  }

  return <>{children}</>;
}