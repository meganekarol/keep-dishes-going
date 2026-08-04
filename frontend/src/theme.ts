import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    primary: { main: '#c2410c' },
    secondary: { main: '#0f766e' },
    background: { default: '#faf7f5' },
  },
  shape: { borderRadius: 12 },
  typography: {
    h1: { fontSize: '2.25rem', fontWeight: 700 },
    h2: { fontSize: '1.5rem', fontWeight: 700 },
  },
});
