import { useQuery } from '@tanstack/react-query';
import { useAuth } from 'react-oidc-context';

import { ownerService } from '../services/ownerService';

export const ownerKeys = {
  myRestaurant: ['restaurants', 'me'] as const,
};

/**
 * The signed-in owner's restaurant, or null when they have none yet.
 *
 * `enabled` is what keeps the request from leaving before there is a token to
 * put on it: effects run child-first, so this hook's query would otherwise fire
 * ahead of the registration in `useApiToken` and be rejected as anonymous.
 */
export function useMyRestaurant() {
  const auth = useAuth();

  return useQuery({
    queryKey: ownerKeys.myRestaurant,
    queryFn: ownerService.getMyRestaurant,
    enabled: auth.isAuthenticated,
  });
}