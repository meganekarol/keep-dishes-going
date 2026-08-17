import { useCallback, useEffect, useState } from 'react';

/** Just enough to find an order again and label it in a list. */
export interface PlacedOrder {
  orderId: string;
  restaurantName: string;
  placedAt: string;
}

const STORAGE_KEY = 'kdg.orders';

/** Old orders are not worth keeping forever, and the list should stay readable. */
const KEEP_MOST_RECENT = 20;

/**
 * The orders this browser has placed (US-24).
 * Kept deliberately small. The status is not stored, because it changes in the
 * kitchen rather than here; the list asks the server for that.
 */
export function usePlacedOrders() {
  const [orders, setOrders] = useState<PlacedOrder[]>(() => read());

  useEffect(() => {
    const onStorage = (event: StorageEvent) => {
      if (event.key === STORAGE_KEY) setOrders(read());
    };
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const remember = useCallback((order: PlacedOrder) => {
    setOrders(() => {
      const current = read().filter((existing) => existing.orderId !== order.orderId);
      const next = [order, ...current].slice(0, KEEP_MOST_RECENT);
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
  }, []);

  const forget = useCallback((orderId: string) => {
    setOrders(() => {
      const next = read().filter((order) => order.orderId !== orderId);
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
      return next;
    });
  }, []);

  return { orders, remember, forget };
}

/**
 * Reads from storage every time rather than trusting React state, so two
 * checkouts in quick succession cannot overwrite each other.
 */
function read(): PlacedOrder[] {
  const stored = window.localStorage.getItem(STORAGE_KEY);
  if (!stored) return [];

  try {
    const parsed: unknown = JSON.parse(stored);
    if (!Array.isArray(parsed)) return [];
    return parsed.filter(
      (entry): entry is PlacedOrder =>
        typeof entry === 'object' &&
        entry !== null &&
        typeof (entry as PlacedOrder).orderId === 'string',
    );
  } catch {
    return [];
  }
}