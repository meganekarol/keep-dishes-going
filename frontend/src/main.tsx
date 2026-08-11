import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { CssBaseline, ThemeProvider } from '@mui/material';

import App from './App';
import SecurityContextProvider from './context/SecurityContextProvider';
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

// No StrictMode here on purpose. It renders everything twice to surface bugs,
// and Keycloak's init cannot survive being called twice.
createRoot(document.getElementById('root')!).render(
  <QueryClientProvider client={queryClient}>
    <SecurityContextProvider>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </ThemeProvider>
    </SecurityContextProvider>
  </QueryClientProvider>,
);