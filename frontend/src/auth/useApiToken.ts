import { useEffect } from 'react';
import { useAuth } from 'react-oidc-context';
import { setAccessTokenProvider } from '../services/apiClient';

/**
 * Hands the API client a way to read the current access token.
 *
 * The client is a plain module, not a component, so it cannot call `useAuth()`
 * itself. Registering a getter rather than the token value means requests always
 * read the token that is current when they are sent, not the one that happened
 * to be in scope when the component last rendered.
 *
 * <p>The effect is here because this is the one thing effects are actually for:
 * synchronising with something outside React. It is not the "transform data for
 * rendering" or "handle a user event" kind of effect that should be deleted.
 *
 * <p>One ordering caveat it cannot solve on its own: effects run child-first, so
 * a query started by a child on the very first commit fires before this
 * registration does, and would go out without a token. Owner queries therefore
 * wait for the session themselves rather than relying on the timing here.
 */
export function useApiToken(): void {
  const auth = useAuth();

  useEffect(() => {
    setAccessTokenProvider(() => auth.user?.access_token);
  }, [auth]);
}
