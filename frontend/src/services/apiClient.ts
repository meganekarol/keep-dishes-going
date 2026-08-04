import type { ErrorResponse } from '../types/api';

const BASE_URL = import.meta.env.VITE_API_BASE_URL;

/**
 * A failed request, carrying the status so callers can branch on it — a 404 on a
 * tracking page means "no such order", not "the server is broken".
 */
export class ApiError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

/** Supplied by the auth layer so this module stays unaware of how tokens are obtained. */
let accessTokenProvider: () => string | undefined = () => undefined;

export function setAccessTokenProvider(provider: () => string | undefined): void {
  accessTokenProvider = provider;
}

/**
 * Query parameters, with `undefined`, `null` and `''` dropped rather than sent
 * as empty strings — the backend would try to convert those and answer 400.
 */
export type QueryParams = Record<string, string | number | boolean | string[] | undefined | null>;

function buildQuery(params: QueryParams = {}): string {
  const search = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === '') continue;
    // Sets of enums are sent comma-separated, which is what Spring binds to a Set<E>.
    search.append(key, Array.isArray(value) ? value.join(',') : String(value));
  }

  const query = search.toString();
  return query ? `?${query}` : '';
}

async function toApiError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as ErrorResponse;
    if (body?.message) return new ApiError(response.status, body.message);
  } catch {
    // Not every failure has a JSON body — a 500 from outside the handler, or a
    // network-level rejection. Fall through to the status text.
  }
  return new ApiError(response.status, response.statusText || 'Request failed');
}

async function request<T>(path: string, init: RequestInit = {}, params?: QueryParams): Promise<T> {
  const token = accessTokenProvider();

  const response = await fetch(`${BASE_URL}${path}${buildQuery(params)}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });

  if (!response.ok) throw await toApiError(response);
  if (response.status === 204) return undefined as T;

  return (await response.json()) as T;
}

export const api = {
  get: <T>(path: string, params?: QueryParams) => request<T>(path, { method: 'GET' }, params),

  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),

  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'PATCH',
      ...(body === undefined ? {} : { body: JSON.stringify(body) }),
    }),
};
