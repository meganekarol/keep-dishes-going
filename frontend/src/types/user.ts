/** The signed-in owner, as read from the Keycloak tokens. */
export type User = {
  name: string;
  roles: string[];
};