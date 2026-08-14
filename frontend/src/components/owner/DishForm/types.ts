import type { CreateDishRequest, DishType, DishVersion, FoodTag } from '../../../types/api';

/**
 * What the form holds while it is being filled in.
 *
 * The price stays a string so a cleared field reads as empty instead of
 * collapsing to 0, and the type starts empty so the select shows no answer
 * until one is picked. Same reasoning as the restaurant form.
 */
export interface DishFormValues {
  name: string;
  type: DishType | '';
  foodTags: FoodTag[];
  description: string;
  price: string;
  pictureUrl: string;
}

export const DISH_TYPES: DishType[] = ['STARTER', 'MAIN', 'DESSERT'];

export const FOOD_TAGS: FoodTag[] = ['GLUTEN_FREE', 'LACTOSE_FREE', 'VEGAN', 'SPICY'];

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