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
  open: boolean;
  openingHours: WeeklyOpeningHours;
  menu: MenuItem[];
}

export type RestaurantStatus = 'OPEN' | 'CLOSED' | 'MANUALLY_OPENED' | 'MANUALLY_CLOSED';

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
}

/** The one error body every endpoint returns. */
export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  timestamp: string;
}
