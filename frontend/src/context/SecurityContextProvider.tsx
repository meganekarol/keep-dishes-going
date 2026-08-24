import { type PropsWithChildren, useEffect, useState } from 'react';
import Keycloak from 'keycloak-js';
import { isExpired } from 'react-jwt';

import SecurityContext from './SecurityContext';
import { addAccessTokenToAuthHeader, removeAccessTokenFromAuthHeader } from '../services/auth';
import type { User } from '../types/user';

const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
};

const keycloak: Keycloak = new Keycloak(keycloakConfig);

/**
 * Initialises Keycloak once and hands the session to the rest of the app.
 *
 * Uses check-sso rather than login-required: customers browse and order without
 * an account (US-30), so this only reports whether someone is signed in. The
 * route guard is what starts a login, and only for the owner area.
 */
export default function SecurityContextProvider({ children }: PropsWithChildren) {
  const [loggedInUser, setLoggedInUser] = useState<User | undefined>(undefined);
  const [isInitialised, setIsInitialised] = useState(false);

  useEffect(() => {
    keycloak.init({ onLoad: 'check-sso' });
  }, []);

  keycloak.onReady = () => {
    setIsInitialised(true);
  };

  keycloak.onAuthSuccess = () => {
    addAccessTokenToAuthHeader(keycloak.token);
    updateUserFromToken();
  };

  keycloak.onAuthLogout = () => {
    removeAccessTokenFromAuthHeader();
    setLoggedInUser(undefined);
  };

  keycloak.onAuthError = () => {
    removeAccessTokenFromAuthHeader();
  };

  keycloak.onTokenExpired = () => {
    keycloak.updateToken(-1).then(function () {
      addAccessTokenToAuthHeader(keycloak.token);
      updateUserFromToken();
    });
  };

  function login() {
    keycloak.login();
  }

  /**
   * Opens Keycloak's registration page (US-01).
   *
   * Only owners ever sign up, so the realm gives every new account the owner
   * role by default, KDG's own account aside. Customers order as guests and
   * never register.
   */
  function register() {
    keycloak.register({ redirectUri: `${window.location.origin}/owner` });
  }

  function logout() {
    keycloak.logout({ redirectUri: window.location.origin });
  }

  function isAuthenticated() {
    if (keycloak.token) return !isExpired(keycloak.token);
    else return false;
  }

  function updateUserFromToken() {
    if (!keycloak.idTokenParsed || !keycloak.tokenParsed) return;

    const name = keycloak.idTokenParsed.given_name ?? keycloak.idTokenParsed.preferred_username;
    const realmRoles = keycloak.tokenParsed.realm_access?.roles ?? [];

    setLoggedInUser({
      name,
      roles: realmRoles,
    });
  }

  return (
    <SecurityContext.Provider
      value={{
        isInitialised,
        isAuthenticated,
        loggedInUser,
        login,
        register,
        logout,
      }}
    >
      {children}
    </SecurityContext.Provider>
  );
}