import { z } from 'zod';

import type { CreateDishRequest, DishType, DishVersion, FoodTag } from '../../../types/api';

export const DISH_TYPES = ['STARTER', 'MAIN', 'DESSERT'] as const satisfies readonly DishType[];

export const FOOD_TAGS = [
  'GLUTEN_FREE',
  'LACTOSE_FREE',
  'VEGAN',
  'SPICY',
] as const satisfies readonly FoodTag[];

/**
 * What the form holds while it is being filled in (US-03).
 *
 * The price stays a string so a cleared field reads as empty instead of
 * collapsing to 0, and the type starts empty so the select shows no answer
 * until one is picked. Same reasoning as the restaurant form.
 */
export const dishFormSchema = z.object({
  name: z.string().trim().min(1, 'A name is required'),
  type: z.union([z.enum(DISH_TYPES), z.literal('')]).superRefine((type, ctx) => {
    if (type === '') ctx.addIssue({ code: 'custom', message: 'Pick a course' });
  }),

  foodTags: z.array(z.enum(FOOD_TAGS)),

  description: z.string().trim().min(1, 'A description is required'),

  price: z
    .string()
    .min(1, 'A price is required')
    .refine((price) => Number(price) >= 0.01, { message: 'A dish has to cost something' }),

  // Optional, so an empty box passes and only a filled one has to look like a URL.
  pictureUrl: z
    .string()
    .trim()
    .refine((url) => url === '' || /^https?:\/\/.+/.test(url), {
      message: 'Must start with http:// or https://',
    }),
});

export type DishFormValues = z.infer<typeof dishFormSchema>;

export const EMPTY_DISH_FORM: DishFormValues = {
  name: '',
  type: '',
  foodTags: [],
  description: '',
  price: '',
  pictureUrl: '',
};

/** Prefills the form with a version that already exists, for the edit screen. */
export function toDishFormValues(version: DishVersion): DishFormValues {
  return {
    name: version.name,
    type: version.type,
    foodTags: version.foodTags,
    description: version.description,
    price: String(version.price),
    pictureUrl: version.pictureUrl ?? '',
  };
}

/**
 * Turns what the owner typed into what the API expects.
 *
 * Creating a dish and editing one take the same body, so both pages send this.
 */
export function toDishRequest(values: DishFormValues): CreateDishRequest {
  const pictureUrl = values.pictureUrl.trim();

  return {
    name: values.name.trim(),
    // Never empty by the time we get here: the form requires a choice.
    type: values.type as DishType,
    foodTags: values.foodTags,
    description: values.description.trim(),
    price: Number(values.price),
    pictureUrl: pictureUrl || null,
  };
}