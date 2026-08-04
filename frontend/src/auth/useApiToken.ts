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
 */
export function useApiToken(): void {
  const auth = useAuth();

  useEffect(() => {
    setAccessTokenProvider(() => auth.user?.access_token);
  }, [auth]);
}
