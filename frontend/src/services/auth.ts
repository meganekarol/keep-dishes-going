/**
 * Holds the access token for outgoing requests.
 *
 * The token stays in memory on purpose. Putting it in localStorage would leave
 * it readable by any injected script, and it is not needed there: Keycloak keeps
 * an httpOnly SSO cookie of its own, so a reload can silently get a fresh token
 * through check-sso instead of reading a stored one.
 */
let accessToken: string | undefined;

export function addAccessTokenToAuthHeader(token: string | undefined): void {
  if (token) accessToken = token;
  else removeAccessTokenFromAuthHeader();
}

export function removeAccessTokenFromAuthHeader(): void {
  accessToken = undefined;
}

/** The Authorization header to merge into a request, empty when signed out. */
export function authHeader(): Record<string, string> {
  return accessToken ? { Authorization: `Bearer ${accessToken}` } : {};
}