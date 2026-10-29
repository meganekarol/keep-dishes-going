import { useContext } from 'react';
import { useQuery } from '@tanstack/react-query';

import SecurityContext from '../context/SecurityContext';
import { ownerService } from '../services/ownerService';

export const ownerKeys = {
  myRestaurant: ['restaurants', 'me'] as const,
};

/**
 * The signed-in owner's restaurant, or null when they have none yet.
 *
 * `enabled` keeps the request from leaving before there is a token to put on it,
 * which would come back 401.
 */
export function useMyRestaurant() {
  const { isAuthenticated } = useContext(SecurityContext);

  return useQuery({
    queryKey: ownerKeys.myRestaurant,
    queryFn: ownerService.getMyRestaurant,
    enabled: isAuthenticated(),
  });
}