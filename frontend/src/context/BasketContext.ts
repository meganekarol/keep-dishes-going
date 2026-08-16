import { createContext } from 'react';


export interface BasketLine {
  dishId: string;
  name: string;
  unitPrice: number;
  quantity: number;
}

/**
 * An order goes to exactly one restaurant, so the basket holds one restaurant's
 * dishes. The name is kept alongside the id to title the basket page.
 */
export interface Basket {
  restaurantId: string;
  restaurantName: string;
  lines: BasketLine[];
}

export type BasketContext = {
  basket: Basket | null;
  /** Total number of dishes, quantities included. Drives the navbar badge. */
  itemCount: number;
  total: number;
  /** Adding a dish from another restaurant replaces the basket. */
  addItem: (restaurant: { restaurantId: string; restaurantName: string }, line: BasketLine) => void;
  setQuantity: (dishId: string, quantity: number) => void;
  removeItem: (dishId: string) => void;
  clear: () => void;
};

export default createContext<BasketContext>({
  basket: null,
  itemCount: 0,
  total: 0,
  addItem: () => {},
  setQuantity: () => {},
  removeItem: () => {},
  clear: () => {},
});