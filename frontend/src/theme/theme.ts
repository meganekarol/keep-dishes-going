import { createTheme } from '@mui/material/styles';

import { accent, brand, caution, negative, neutral, positive, shadow, tint } from './colors';

const HEADING_FONT = '"Outfit", "Segoe UI", system-ui, sans-serif';
const BODY_FONT = '"DM Sans", "Segoe UI", system-ui, sans-serif';

/**
 * The one place the look is decided (US-13).
 *
 * Component defaults live here rather than in each screen's `sx`, so a card on
 * the owner's menu and a card in the catalogue cannot drift apart. Both colour
 * schemes are defined, so a screen never has to ask which one is showing.
 */
export const theme = createTheme({
  // A class on the root element rather than a media query, so the toggle can
  // override what the operating system asked for.
  cssVariables: { colorSchemeSelector: 'class' },

  colorSchemes: {
    light: {
      palette: {
        primary: {
          main: brand[700],
          light: tint.brandLight,
          dark: brand[800],
          contrastText: '#FFFFFF',
        },
        secondary: {
          main: accent[700],
          light: tint.accentLight,
          dark: accent[800],
          contrastText: '#FFFFFF',
        },
        success: { main: positive[700], light: positive.light },
        warning: { main: caution[700], light: caution.light },
        error: { main: negative[700], light: negative.light },
        background: { default: '#FBF8F6', paper: '#FFFFFF' },
        text: { primary: neutral[900], secondary: neutral[600] },
        divider: '#EBE4DE',
      },
    },

    dark: {
      palette: {
        // A step lighter than light mode's rust, which goes muddy on a dark
        // ground. The tints invert: `light` is a background, so on dark it has
        // to be dark, and `dark` stays dark because photography sits on it.
        primary: {
          main: brand[500],
          light: tint.brandDark,
          dark: brand[900],
          contrastText: neutral[950],
        },
        secondary: {
          main: accent[400],
          light: tint.accentDark,
          dark: accent[900],
          contrastText: neutral[950],
        },
        success: { main: positive[400], light: positive.dark },
        warning: { main: caution[400], light: caution.dark },
        error: { main: negative[400], light: negative.dark },
        background: { default: neutral[950], paper: neutral[900] },
        text: { primary: neutral[50], secondary: neutral[400] },
        divider: neutral[800],
      },
    },
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
        // theme.vars, not theme.palette: inside styleOverrides the palette
        // resolves to one scheme's literal value, so the bar would keep the
        // light colour after the toggle. The vars are the switching ones.
        root: ({ theme }) => ({
          backgroundColor: (theme.vars ?? theme).palette.background.paper,
          borderBottom: `1px solid ${(theme.vars ?? theme).palette.divider}`,
          // MUI tints raised surfaces in dark mode, which would lighten the bar
          // back towards the colour we just moved away from.
          backgroundImage: 'none',
        }),
      },
    },

    MuiCard: {
      defaultProps: { elevation: 0 },
      styleOverrides: {
        root: ({ theme }) => ({
          border: `1px solid ${(theme.vars ?? theme).palette.divider}`,
          borderRadius: 16,
          // Images sit flush against the card edge, so they have to be clipped
          // by the same radius.
          overflow: 'hidden',
          boxShadow: shadow.resting,
          transition: 'transform 180ms ease, box-shadow 180ms ease',

          // Only cards you can actually click lift. A summary card that just
          // reports something must not pretend to be a button.
          '&:has(.MuiCardActionArea-root):hover': {
            transform: 'translateY(-3px)',
            boxShadow: shadow.lifted,
          },

          '@media (prefers-reduced-motion: reduce)': {
            transition: 'box-shadow 180ms ease',
            '&:has(.MuiCardActionArea-root):hover': { transform: 'none' },
          },

          ...theme.applyStyles('dark', {
            boxShadow: shadow.restingDark,
            '&:has(.MuiCardActionArea-root):hover': { boxShadow: shadow.liftedDark },
          }),
        }),
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
      styleOverrides: {
        root: ({ theme }) => ({
          borderRadius: 10,
          backgroundColor: (theme.vars ?? theme).palette.background.paper,
        }),
      },
    },

    MuiToggleButton: {
      styleOverrides: { root: { textTransform: 'none', fontWeight: 500 } },
    },

    MuiAlert: {
      styleOverrides: { root: { borderRadius: 12 } },
    },
  },
});
