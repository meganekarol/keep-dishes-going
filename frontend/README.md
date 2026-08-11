# Keep Dishes Going — frontend

React + TypeScript client for the Keep Dishes Going backend.

## Running it

The backend and its infrastructure have to be up first:

```bash
cd ../backend/infrastructure && docker compose up -d
cd ..                        && ./gradlew bootRun
```

Then:

```bash
npm install
npm run dev      # http://localhost:5173
```

`npm run build` type-checks and bundles; `npm run lint` runs oxlint.

## Configuration

`.env` holds the local addresses the API, and the Keycloak realm the owner
login goes through. It is committed because none of it is secret: the Keycloak
client is public, so it has no secret to keep. Put local overrides in
`.env.local`, which git ignores.

The backend only allows `http://localhost:5173` through CORS, so the dev server
has to stay on its default port.

## Layout

```
src/
  components/  Reusable pieces: cards, filters, the layout shell, the route guard
  context/     Keycloak setup, exposed to the app as a security context
  hooks/       React Query hooks; the rest of the app calls these, not the services
  pages/       One component per route
  services/    The HTTP layer: one module per backend context, plus the bearer token
  types/       The backend's read models, mirrored in TypeScript
  utils/       Formatting
```

The layering runs `pages → hooks → services → apiClient`. Components never
fetch, and nothing outside `services/` builds a URL.

## Notes

**Customers are not logged in.** Browsing, ordering and tracking are all open
(US-30), so the login button is there for restaurant owners only. Keycloak is
therefore initialised with `check-sso`, which reports whether someone is signed
in without forcing a login; `RequireOwner` is what starts one, and only for the
owner area. The API client attaches a token when one exists and sends the
request without it otherwise.

**Tokens are never written to storage.** They stay in memory in
`services/auth.ts`, so an injected script cannot read them back. Nothing is lost
by that: Keycloak keeps its own httpOnly SSO cookie, so a page reload gets a
fresh token through `check-sso` rather than by reading a stored one.

**No `StrictMode`.** It renders everything twice, and Keycloak's `init` cannot
survive being called twice.

**Tracking polls.** The restaurant accepts and prepares the order on its own
schedule with no way to push that to the browser, so the tracking page refetches
every five seconds and stops once the order is delivered or rejected.

**Filtering and sorting happen server-side.** The price filter means "has at
least one dish in this range", which the client cannot work out from a summary,
and the dish sort orders are defined by the backend's `DishSortOrder` keeping
both there avoids a second, drifting copy of the rules.
