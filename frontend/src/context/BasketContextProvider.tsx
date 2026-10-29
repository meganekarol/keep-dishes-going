import { type PropsWithChildren, useEffect, useMemo, useReducer } from 'react';

import BasketContext, { type Basket, type BasketLine } from './BasketContext';

const STORAGE_KEY = 'kdg.basket';

type BasketAction =
  | { type: 'add'; restaurantId: string; restaurantName: string; line: BasketLine }
  | { type: 'setQuantity'; dishId: string; quantity: number }
  | { type: 'remove'; dishId: string }
  | { type: 'clear' };

function reduce(basket: Basket | null, action: BasketAction): Basket | null {
  switch (action.type) {
    case 'add': {
      // Dishes from another restaurant cannot share an order, so the basket
      // starts over. The page warns before this runs.
      const current =
        basket && basket.restaurantId === action.restaurantId
          ? basket
          : { restaurantId: action.restaurantId, restaurantName: action.restaurantName, lines: [] };

      const existing = current.lines.find((line) => line.dishId === action.line.dishId);
      const lines = existing
        ? current.lines.map((line) =>
            line.dishId === action.line.dishId
              ? { ...line, quantity: line.quantity + action.line.quantity }
              : line,
          )
        : [...current.lines, action.line];

      return { ...current, lines };
    }

    case 'setQuantity': {
      if (!basket) return null;
      const lines =
        action.quantity < 1
          ? basket.lines.filter((line) => line.dishId !== action.dishId)
          : basket.lines.map((line) =>
              line.dishId === action.dishId ? { ...line, quantity: action.quantity } : line,
            );
      return lines.length === 0 ? null : { ...basket, lines };
    }

    case 'remove': {
      if (!basket) return null;
      const lines = basket.lines.filter((line) => line.dishId !== action.dishId);
      return lines.length === 0 ? null : { ...basket, lines };
    }

    case 'clear':
      return null;
  }
}


function readStoredBasket(): Basket | null {
  const stored = window.localStorage.getItem(STORAGE_KEY);
  if (!stored) return null;

  try {
    const parsed: unknown = JSON.parse(stored);
    if (
      typeof parsed !== 'object' ||
      parsed === null ||
      typeof (parsed as Basket).restaurantId !== 'string' ||
      !Array.isArray((parsed as Basket).lines)
    ) {
      return null;
    }
    return (parsed as Basket).lines.length === 0 ? null : (parsed as Basket);
  } catch {
    return null;
  }
}

/**
 * Keeps the basket for the whole session (US-20).
 *
 * It lives in localStorage rather than React state alone: a customer who
 * reloads, or follows a link back to the menu, should not lose what they picked.
 */
export default function BasketContextProvider({ children }: PropsWithChildren) {
  const [basket, dispatch] = useReducer(reduce, null, readStoredBasket);

  useEffect(() => {
    if (basket) window.localStorage.setItem(STORAGE_KEY, JSON.stringify(basket));
    else window.localStorage.removeItem(STORAGE_KEY);
  }, [basket]);

  // Memoised because this is a context value. A fresh object on every render
  // would re-render every basket consumer, which is the whole tree below the
  // navbar, whether or not the basket actually changed.
  const value = useMemo(() => {
    const lines = basket?.lines ?? [];
    return {
      basket,
      itemCount: lines.reduce((count, line) => count + line.quantity, 0),
      total: lines.reduce((sum, line) => sum + Math.round(line.unitPrice * 100) * line.quantity, 0) / 100,
      addItem: (restaurant: { restaurantId: string; restaurantName: string }, line: BasketLine) =>
        dispatch({ type: 'add', ...restaurant, line }),
      setQuantity: (dishId: string, quantity: number) =>
        dispatch({ type: 'setQuantity', dishId, quantity }),
      removeItem: (dishId: string) => dispatch({ type: 'remove', dishId }),
      clear: () => dispatch({ type: 'clear' }),
    };
  }, [basket]);

  return <BasketContext.Provider value={value}>{children}</BasketContext.Provider>;
}