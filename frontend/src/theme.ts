import { createTheme } from '@mui/material/styles';

const HEADING_FONT = '"Outfit", "Segoe UI", system-ui, sans-serif';
const BODY_FONT = '"DM Sans", "Segoe UI", system-ui, sans-serif';

/** Warm neutrals rather than grey, so the food photography sits on a kitchen palette. */
const INK = '#1C1917';
const MUTED = '#57534E';
const RULE = '#EBE4DE';

/**
 * The one place the look is decided (US-13).
 *
 * Component defaults live here rather than in each screen's `sx`, so a card on
 * the owner's menu and a card in the catalogue cannot drift apart.
 */
export const theme = createTheme({
  palette: {
    primary: { main: '#C2410C', light: '#FFF1E9', dark: '#9A3412', contrastText: '#FFFFFF' },
    secondary: { main: '#0F766E', light: '#E4F1EF', dark: '#115E56', contrastText: '#FFFFFF' },
    success: { main: '#15803D', light: '#E8F5EC' },
    warning: { main: '#B45309', light: '#FDF3E3' },
    error: { main: '#B91C1C', light: '#FCEBEA' },
    background: { default: '#FBF8F6', paper: '#FFFFFF' },
    text: { primary: INK, secondary: MUTED },
    divider: RULE,
  },

  shape: { borderRadius: 12 },

  typography: {
    fontFamily: BODY_FONT,
    h1: {
      fontFamily: HEADING_FONT,
      fontSize: '2.5rem',
      fontWeight: 700,
      lineHeight: 1.1,
      letterSpacing: '-0.02em',
    },
    h2: {
      fontFamily: HEADING_FONT,
      fontSize: '1.5rem',
      fontWeight: 600,
      lineHeight: 1.25,
      letterSpacing: '-0.01em',
    },
    h3: { fontFamily: HEADING_FONT, fontSize: '1.2rem', fontWeight: 600, lineHeight: 1.3 },
    h6: { fontFamily: HEADING_FONT, fontWeight: 700, letterSpacing: '-0.01em' },
    subtitle1: { fontWeight: 600 },
    body2: { lineHeight: 1.6 },
    button: { textTransform: 'none', fontWeight: 600 },
    overline: { fontWeight: 600, letterSpacing: '0.1em' },
  },

  components: {
    MuiAppBar: {
      defaultProps: { color: 'inherit', elevation: 0 },
      styleOverrides: {
        root: { backgroundColor: '#FFFFFF', borderBottom: `1px solid ${RULE}` },
      },
    },

    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: {
          border: `1px solid ${RULE}`,
          borderRadius: 16,
          // Images sit flush against the card edge, so they have to be clipped
          // by the same radius.
          overflow: 'hidden',
        },
      },
    },

    MuiButton: {
      defaultProps: { disableElevation: true },
      styleOverrides: {
        root: { borderRadius: 10 },
        sizeLarge: { paddingInline: 24, paddingBlock: 10 },
      },
    },

    MuiChip: {
      styleOverrides: {
        root: { fontWeight: 500 },
        sizeSmall: { height: 24 },
      },
    },

    MuiOutlinedInput: {
      styleOverrides: { root: { borderRadius: 10, backgroundColor: '#FFFFFF' } },
    },

    MuiToggleButton: {
      styleOverrides: { root: { textTransform: 'none', fontWeight: 500 } },
    },

    MuiAlert: {
      styleOverrides: { root: { borderRadius: 12 } },
    },
  },
});