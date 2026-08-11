import type { ReactNode } from 'react';
import { Alert, Box, CircularProgress } from '@mui/material';

import { ApiError } from '../services/apiClient';

interface QueryStateProps {
  isLoading: boolean;
  error: unknown;
  children: ReactNode;
}

/**
 * The loading spinner and error banner every page would otherwise repeat.
 *
 * Errors from the API already carry a message written to be shown to a user,
 * guaranteed by the backend's `GlobalExceptionHandler`, so it is displayed as
 * given rather than replaced with something vaguer.
 */
export function QueryState({ isLoading, error, children }: QueryStateProps) {
  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    const message =
      error instanceof ApiError || error instanceof Error
        ? error.message
        : 'Something went wrong. Please try again.';
    return <Alert severity="error">{message}</Alert>;
  }

  return <>{children}</>;
}
