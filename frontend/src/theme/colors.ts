/**
 * The design tokens the palette is built from (US-13).
 *
 * Numbered scales rather than three loose constants, so light and dark can pick
 * different steps of the same hue instead of inventing new colours. The rust and
 * teal the app already used are steps 700 of `brand` and `accent`; the warm
 * greys are what keeps food photography from sitting on a cold background.
 */

/** Rust. The platform's own colour. */
export const brand = {
  50: '#FFF7ED',
  100: '#FFEDD5',
  200: '#FED7AA',
  300: '#FDBA74',
  400: '#FB923C',
  500: '#F97316',
  600: '#EA580C',
  700: '#C2410C',
  800: '#9A3412',
  900: '#7C2D12',
  950: '#431407',
} as const;

/** Teal, used for the owner's side of the app. */
export const accent = {
  50: '#F0FDFA',
  100: '#CCFBF1',
  200: '#99F6E4',
  300: '#5EEAD4',
  400: '#2DD4BF',
  500: '#14B8A6',
  600: '#0D9488',
  700: '#0F766E',
  800: '#115E59',
  900: '#134E4A',
  950: '#042F2E',
} as const;

/** Warm neutrals. A cold grey would fight the food. */
export const neutral = {
  50: '#FAFAF9',
  100: '#F5F5F4',
  200: '#E7E5E4',
  300: '#D6D3D1',
  400: '#A8A29E',
  500: '#78716C',
  600: '#57534E',
  700: '#44403C',
  800: '#292524',
  900: '#1C1917',
  950: '#0C0A09',
} as const;

export const positive = { 700: '#15803D', light: '#E8F5EC' } as const;
export const caution = { 700: '#B45309', light: '#FDF3E3' } as const;
export const negative = { 700: '#B91C1C', light: '#FCEBEA' } as const;

/**
 * Tinted surfaces, used as backgrounds behind an icon or a highlighted row.
 */
export const tint = {
  brandLight: '#FFF1E9',
  accentLight: '#E4F1EF',
} as const;

/** Built from the ink rather than black, so shadows warm up rather than grey out. */
export const shadow = {
  resting: '0 1px 2px rgba(28,25,23,.04), 0 8px 20px -12px rgba(28,25,23,.10)',
  lifted: '0 2px 4px rgba(28,25,23,.06), 0 18px 36px -16px rgba(28,25,23,.22)',
} as const;
