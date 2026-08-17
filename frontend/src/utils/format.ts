const currency = new Intl.NumberFormat('nl-BE', { style: 'currency', currency: 'EUR' });

const dateTime = new Intl.DateTimeFormat('nl-BE', { dateStyle: 'short', timeStyle: 'short' });

const clock = new Intl.DateTimeFormat('nl-BE', { hour: '2-digit', minute: '2-digit' });

export function formatPrice(value: number | null | undefined): string {
  return value === null || value === undefined ? '—' : currency.format(value);
}

export function formatPriceRange(lowest: number | null, highest: number | null): string {
  if (lowest === null || highest === null) return 'No dishes yet';
  return lowest === highest ? formatPrice(lowest) : `${formatPrice(lowest)} – ${formatPrice(highest)}`;
}

/** The backend sends `LocalDateTime`, i.e. no zone: it is already local time. */
export function formatDateTime(value: string | null | undefined): string {
  return value ? dateTime.format(new Date(value)) : '—';
}

/**
 * The clock time out of a `LocalDateTime`, e.g. `19:45`.
 *
 * Distinct from `formatTime` below, which trims a `LocalTime`. Feeding a full
 * timestamp to that one returns the first five characters of the date.
 */
export function formatClockTime(value: string | null | undefined): string {
  return value ? clock.format(new Date(value)) : '—';
}

/** Trims `LocalTime`'s seconds: `18:30:00` reads better as `18:30`. */
export function formatTime(value: string): string {
  return value.slice(0, 5);
}

/** `GLUTEN_FREE` → `Gluten free`. */
export function humanise(value: string): string {
  const spaced = value.replace(/_/g, ' ').toLowerCase();
  return spaced.charAt(0).toUpperCase() + spaced.slice(1);
}
