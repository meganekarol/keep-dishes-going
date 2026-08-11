import type { CuisineType, DayOfWeek } from '../../../types/api';

/** One day's row in the weekly schedule. Unchecked days are left out on submit. */
export interface OpeningHoursDayValues {
  enabled: boolean;
  open: string;
  closed: string;
}

/**
 * What the form holds while it is being filled in.
 *
 * Numbers are kept as strings so a cleared field stays empty instead of
 * collapsing to 0, and picture URLs are objects because useFieldArray needs a
 * stable key per row. Converting all of that into the backend's request shape is
 * the caller's job.
 */
export interface RestaurantFormValues {
  name: string;
  address: {
    street: string;
    number: string;
    postalCode: string;
    city: string;
    country: string;
  };
  contactEmail: string;
  cuisineType: CuisineType | '';
  defaultPreparationTime: string;
  pictureUrls: { url: string }[];
  openingHours: Record<DayOfWeek, OpeningHoursDayValues>;
}

export const WEEK: DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
];

export const CUISINE_TYPES: CuisineType[] = [
  'BELGIAN',
  'CHINESE',
  'FRENCH',
  'FUSION',
  'INDIAN',
  'ITALIAN',
  'JAPANESE',
  'KOREAN',
  'MEXICAN',
  'THAI',
];

function emptyDay(): OpeningHoursDayValues {
  return { enabled: false, open: '11:30', closed: '22:00' };
}

export const EMPTY_RESTAURANT_FORM: RestaurantFormValues = {
  name: '',
  address: { street: '', number: '', postalCode: '', city: '', country: 'Belgium' },
  contactEmail: '',
  cuisineType: '',
  defaultPreparationTime: '20',
  pictureUrls: [{ url: '' }],
  openingHours: {
    MONDAY: emptyDay(),
    TUESDAY: emptyDay(),
    WEDNESDAY: emptyDay(),
    THURSDAY: emptyDay(),
    FRIDAY: emptyDay(),
    SATURDAY: emptyDay(),
    SUNDAY: emptyDay(),
  },
};