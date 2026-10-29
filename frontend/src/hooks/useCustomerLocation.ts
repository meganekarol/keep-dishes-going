import { useEffect, useState } from 'react';

export interface CustomerLocation {
  lat: number;
  lon: number;
}

const STORAGE_KEY = 'kdg.location';

/**
 * Where the browser thinks the customer is, for the delivery guesstimate (US-19).
 *
 * <p>Asked for once and remembered for the session.
 */
export function useCustomerLocation() {
  const [location, setLocation] = useState<CustomerLocation | null>(() => read());
  const [denied, setDenied] = useState(false);

  useEffect(() => {
    if (location || denied || !navigator.geolocation) return;

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const next = {
          lat: position.coords.latitude,
          lon: position.coords.longitude,
        };
        window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(next));
        setLocation(next);
      },
      // Refused, unavailable or timed out. All three mean the same thing here:
      // quote the estimate against an assumed distance instead.
      () => setDenied(true),
      { timeout: 10_000, maximumAge: 5 * 60_000 },
    );
  }, [location, denied]);

  return { location, denied };
}

function read(): CustomerLocation | null {
  const stored = window.sessionStorage.getItem(STORAGE_KEY);
  if (!stored) return null;
  try {
    const parsed: unknown = JSON.parse(stored);
    if (
      typeof parsed === 'object' &&
      parsed !== null &&
      typeof (parsed as CustomerLocation).lat === 'number' &&
      typeof (parsed as CustomerLocation).lon === 'number'
    ) {
      return parsed as CustomerLocation;
    }
  } catch {
    // Hand-edited or from an older version. Ask again.
  }
  return null;
}