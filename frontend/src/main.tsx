import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from 'react-oidc-context';
import { CssBaseline, ThemeProvider } from '@mui/material';

import App from './App';
import { oidcConfig } from './auth/oidcConfig';
import { theme } from './theme';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // The catalogue changes when a restaurant edits it, not when the user
      // clicks around, so refetching on every window focus is noise.
      refetchOnWindowFocus: false,
      staleTime: 30_000,
      retry: 1,
    },
  },
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider {...oidcConfig}>
      <QueryClientProvider client={queryClient}>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </ThemeProvider>
      </QueryClientProvider>
    </AuthProvider>
  </StrictMode>,
);
