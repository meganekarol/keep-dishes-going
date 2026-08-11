import { WebStorageStateStore } from 'oidc-client-ts';
import type { AuthProviderProps } from 'react-oidc-context';

const authority = `${import.meta.env.VITE_KEYCLOAK_URL}/realms/${import.meta.env.VITE_KEYCLOAK_REALM}`;

/**
 * Authorization Code flow with PKCE against Keycloak.
 *
 * A browser app cannot keep a client secret, so the Keycloak client is public
 * and PKCE is what proves the code was redeemed by whoever asked for it —
 * `oidc-client-ts` enables it by default for public clients.
 *
 * Only restaurant owners ever log in. Customers order as guests (US-30), which
 * is why every catalogue and tracking call works without a token.
 */
export const oidcConfig: AuthProviderProps = {
  authority,
  client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
  redirect_uri: window.location.origin,
  post_logout_redirect_uri: window.location.origin,
  scope: 'openid profile email',
  // Survives a page reload; sessionStorage would log the owner out on every F5.
  userStore: new WebStorageStateStore({ store: window.localStorage }),
  // Strips ?code=…&state=… once the redirect has been processed, so a reload
  // does not try to redeem an already-used authorization code.
  onSigninCallback: () => {
    window.history.replaceState({}, document.title, window.location.pathname);
  },
};
