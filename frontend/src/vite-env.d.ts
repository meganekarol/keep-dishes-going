/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Where the Spring Boot API lives, e.g. `http://localhost:8080`. */
  readonly VITE_API_BASE_URL: string;
  /** Keycloak's base URL, e.g. `http://localhost:8180`. */
  readonly VITE_KEYCLOAK_URL: string;
  readonly VITE_KEYCLOAK_REALM: string;
  /** A public client — no secret, so PKCE is what secures the flow. */
  readonly VITE_KEYCLOAK_CLIENT_ID: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
