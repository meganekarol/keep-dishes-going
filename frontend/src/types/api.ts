/**
 * The backend's read models, mirrored in TypeScript.
 *
 * These are hand-kept copies of the records in `port.in.queries`. The API is
 * the contract between the two repositories, so it is written down here once and
 * the rest of the app depends on this file rather than on inline shapes.
 *
 * `BigDecimal` arrives as a JSON number and `LocalDateTime` as an ISO string
 * without a zone, e.g. `2026-08-04T18:30:00`.
 */

export type CuisineType =
  | 'FUSION'
  | 'ITALIAN'
  | 'FRENCH'
  | 'JAPANESE'
  | 'THAI'
  | 'CHINESE'
  | 'KOREAN'
  | 'MEXICAN'
  | 'INDIAN'
  | 'BELGIAN';

export type DishType = 'STARTER' | 'MAIN' | 'DESSERT';

export type FoodTag = 'GLUTEN_FREE' | 'LACTOSE_FREE' | 'VEGAN' | 'SPICY';

export type DishSortOrder = 'NAME' | 'PRICE_ASC' | 'PRICE_DESC';

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY';

export type OrderStatus =
  | 'PENDING_ACCEPTANCE'
  | 'ACCEPTED'
  | 'REJECTED'
  | 'READY_FOR_PICKUP'
  | 'IN_DELIVERY'
  | 'DELIVERED';

export interface Address {
  street: string;
  number: number;
  postalCode: string;
  city: string;
  country: string;
}

/** `open`/`closed` are local times, `HH:mm:ss`. */
export interface TimeRange {
  open: string;
  closed: string;
}

/** A weekly schedule. Days the restaurant never opens are simply absent. */
export type WeeklyOpeningHours = Partial<Record<DayOfWeek, TimeRange>>;

export interface MenuItem {
  dishId: string;
  restaurantId: string;
  name: string;
  type: DishType;
  foodTags: FoodTag[];
  description: string;
  price: number;
  pictureUrl: string | null;
  /** False for a dish that is on the menu but out of stock. */
  available: boolean;
}

export interface RestaurantSummary {
  restaurantId: string;
  name: string;
  cuisineType: CuisineType;
  address: Address;
  pictureUrls: string[];
  defaultPreparationTime: number;
  /** Cooking plus the ride, in minutes. A guess, not a promise (US-19). */
  guesstimatedDeliveryTime: number;
  open: boolean;
  /** Null when the restaurant has nothing published yet. */
  lowestPrice: number | null;
  highestPrice: number | null;
  menuSize: number;
}

export interface RestaurantDetail {
  restaurantId: string;
  name: string;
  cuisineType: CuisineType;
  address: Address;
  contactEmail: string;
  pictureUrls: string[];
  defaultPreparationTime: number;
  /** Cooking plus the ride, in minutes. A guess, not a promise (US-19). */
  guesstimatedDeliveryTime: number;
  open: boolean;
  openingHours: WeeklyOpeningHours;
  menu: MenuItem[];
}

export type RestaurantStatus = 'OPEN' | 'CLOSED' | 'MANUALLY_OPENED' | 'MANUALLY_CLOSED';

/**
 * The body of POST /api/restaurants (US-02).
 *
 * There is no owner id: the backend takes it from the subject of the bearer
 * token, so the client cannot create a restaurant for somebody else.
 */
export interface CreateRestaurantRequest {
  name: string;
  address: Address;
  contactEmail: string;
  cuisineType: CuisineType;
  defaultPreparationTime: number;
  openingHours: WeeklyOpeningHours;
  pictureUrls: string[];
}

/**
 * The owner's own restaurant, as returned by GET /api/restaurants/me and
 * POST /api/restaurants.
 *
 */
export interface OwnerRestaurant {
  restaurantId: string;
  name: string;
  address: Address;
  contactEmail: string;
  cuisineType: CuisineType;
  defaultPreparationTime: number;
  openingHours: WeeklyOpeningHours;
  pictureUrls: string[];
  status: RestaurantStatus;
}

export type DishState = 'DRAFT' | 'PUBLISHED' | 'UNPUBLISHED' | 'OUT_OF_STOCK';

/** One version of a dish, live or still a draft. */
export interface DishVersion {
  name: string;
  type: DishType;
  foodTags: FoodTag[];
  description: string;
  price: number;
  pictureUrl: string | null;
}

/**
 * One dish as its owner sees it (US-03), from GET /api/{restaurantId}/dishes.
 *
 * `published` is null for a dish that has never gone live, `draft` for one with
 * no unsaved edits. At least one of the two is always set.
 */
export interface OwnerDishView {
  dishId: string;
  status: DishState;
  published: DishVersion | null;
  draft: DishVersion | null;
  /** The draft differs from what customers can order right now. */
  pendingChanges: boolean;
  scheduledPublishAt: string | null;
  lastPublishedAt: string | null;
}

export interface OwnerMenu {
  restaurantId: string;
  dishes: OwnerDishView[];
  pendingChangesCount: number;
  /** Counted from the menu itself, which is what the cap is enforced against. */
  publishedCount: number;
  maxPublishedDishes: number;
}

/** The body of POST /api/{restaurantId}/dishes/new (US-03). */
export interface CreateDishRequest {
  name: string;
  type: DishType;
  foodTags: FoodTag[];
  description: string;
  price: number;
  pictureUrl: string | null;
}

/**
 * The body of PUT /api/{restaurantId}/dishes/{dishId}/draft (US-03).
 *
 * Identical to creating one: an edit replaces the draft wholesale rather than
 * patching single fields.
 */
export type EditDishRequest = CreateDishRequest;

export interface CreateDishResponse extends DishVersion {
  restaurantId: string;
  dishId: string;
}

export type EditDishResponse = DishVersion;

export interface DeliveryAddress {
  street: string;
  number: string;
  postalCode: string;
  city: string;
  country: string;
  /** Derived server-side; present in the response but never sent back. */
  fullAddress?: string;
}

export interface OrderLine {
  dishId: string;
  dishName: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface OrderTracking {
  orderId: string;
  restaurantId: string;
  restaurantName: string;
  status: OrderStatus;
  /** Only set when `status` is `REJECTED`. */
  rejectionReason: string | null;
  customerName: string;
  deliveryAddress: DeliveryAddress;
  lines: OrderLine[];
  totalPrice: number;
  placedAt: string;
  acceptedAt: string | null;
  readyAt: string | null;
  pickedUpAt: string | null;
  deliveredAt: string | null;
  /**
   * Roughly when the food should arrive (US-19), or null once the order has
   * stopped moving because it was delivered or turned away.
   */
  estimatedDeliveryAt: string | null;
}

/**
 * One order as its restaurant sees it (US-10), from
 * GET /api/restaurants/{restaurantId}/orders.
 *
 * Less than the customer's tracking view: a kitchen needs the dishes, the total
 * and how long the order has been waiting, not the courier's timestamps.
 */
export interface RestaurantOrderSummary {
  orderId: string;
  status: OrderStatus;
  customerName: string;
  deliveryAddress: string;
  /** The ordered dishes as one line, e.g. `2x Frieten, 1x Stoofvlees`. */
  itemSummary: string;
  itemCount: number;
  totalPrice: number;
  /** What the five-minute acceptance deadline counts from. */
  placedAt: string;
  acceptedAt: string | null;
  readyAt: string | null;
  rejectionReason: string | null;
}

/** The one error body every endpoint returns. */
export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
}
