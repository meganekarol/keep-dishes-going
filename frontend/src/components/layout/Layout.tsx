import type { ReactNode } from 'react';
import { Box, Container } from '@mui/material';

import { NavBar } from './NavBar';

export function Layout({ children }: { children: ReactNode }) {
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <NavBar />
      <Container maxWidth="lg" sx={{ py: 4 }}>
        {children}
      </Container>
    </Box>
  );
}
