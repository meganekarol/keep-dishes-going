import { z } from 'zod';

import type { CuisineType, DayOfWeek } from '../../../types/api';

export const WEEK = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
] as const satisfies readonly DayOfWeek[];

export const CUISINE_TYPES = [
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
] as const satisfies readonly CuisineType[];

const TIME = /^\d{2}:\d{2}$/;

/**
 * One day's row in the weekly schedule.
 *
 * The times only have to make sense on a day the restaurant is actually open,
 * which is why the check hangs off the whole row rather than off `closed`.
 */
const daySchema = z
  .object({
    enabled: z.boolean(),
    open: z.string().regex(TIME, 'Required'),
    closed: z.string().regex(TIME, 'Required'),
  })
  .refine((day) => !day.enabled || day.closed > day.open, {
    message: 'Must be after opening',
    path: ['closed'],
  });

/**
 * What the form holds while it is being filled in (US-02).
 *
 * Numbers stay strings so a cleared field stays empty instead of collapsing to
 * 0, and picture URLs are objects because useFieldArray needs a stable key per
 * row. Turning all of that into the backend's request shape is the caller's job.
 */
export const restaurantFormSchema = z.object({
  name: z.string().trim().min(1, 'A name is required'),

  address: z.object({
    street: z.string().trim().min(1, 'Street is required'),
    // The backend stores this as an int, so anything else fails there.
    number: z.string().regex(/^\d+$/, 'Digits only'),
    postalCode: z.string().trim().min(1, 'Postal code is required'),
    city: z.string().trim().min(1, 'City is required'),
    country: z.string().trim().min(1, 'Country is required'),
  }),

  contactEmail: z.string().trim().regex(/^\S+@\S+\.\S+$/, 'That is not an email address'),

  // superRefine rather than refine: a refine that tests `!== ''` narrows the
  // inferred type, and the form starts with '' selected.
  cuisineType: z.union([z.enum(CUISINE_TYPES), z.literal('')]).superRefine((cuisine, ctx) => {
    if (cuisine === '') ctx.addIssue({ code: 'custom', message: 'Pick a cuisine' });
  }),

  defaultPreparationTime: z
    .string()
    .regex(/^\d+$/, 'A preparation time is required')
    .refine((minutes) => Number(minutes) >= 1, { message: 'Must be at least a minute' }),

  pictureUrls: z
    .array(
      z.object({
        url: z.string().trim().regex(/^https?:\/\/.+/, 'Must start with http:// or https://'),
      }),
    )
    .min(1),

  openingHours: z.object({
    MONDAY: daySchema,
    TUESDAY: daySchema,
    WEDNESDAY: daySchema,
    THURSDAY: daySchema,
    FRIDAY: daySchema,
    SATURDAY: daySchema,
    SUNDAY: daySchema,
  }),
});

export type RestaurantFormValues = z.infer<typeof restaurantFormSchema>;

function emptyDay() {
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
