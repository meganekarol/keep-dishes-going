# Keep Dishes Going — frontend

React + TypeScript client for the Keep Dishes Going backend.

## Running it

The backend and its infrastructure have to be up first:

```bash
cd ../backend/infrastructure && docker compose up -d
cd ..                        && ./gradlew bootRun
```

Setting up from scratch, including Keycloak, Stripe and demo data, is written up once in the
backend repository at `documentation/getting-started.md`.

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
  components/
    auth/      the route guard
    catalogue/ the customer-facing cards and filters
    form/      React Hook Form bindings for MUI inputs
    layout/    the app shell and nav bar
    owner/     the management area's pieces
    shared/    used by more than one area
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

---

## Challenges & Accomplishments

### Challenges

**1. Understanding the React concepts and actually using them.**
Hooks, context and React Query each took me a while before I understood when I was supposed to
reach for which one. Zod was the hardest. In the beginning I wrote the validation directly on
every input, because that was the part I understood, and only later moved it all into schemas.
Once it clicked the forms got a lot shorter, and the TypeScript type now comes from the schema
instead of me writing the same thing twice.

**2. Styling the app.**
I am not good at UI design, so this took me longer than writing the code did.

### What I am proud of

The layers stay separate. Components never fetch anything themselves and only the `services`
folder knows any URLs. Everything goes `pages -> hooks -> services -> apiClient`. It made adding
the price range feature easy, because I only had to add one service, one hook and one component
and nothing else had to change.

---

## ✅ Finished Features

- [x] US-13 Landing page offering the customer route, with live restaurant photography
- [x] US-14 Restaurants as a list and on a Leaflet map
- [x] US-15 Restaurant detail page with its menu
- [x] US-16 Filter restaurants by cuisine, price range, distance and delivery time
- [x] US-17 Filter dishes by course and food tags
- [x] US-18 Sort dishes by name or price
- [x] US-19 Guesstimated delivery time, using the browser's location when allowed
- [x] US-20 Basket for a single restaurant, kept in localStorage
- [x] US-21 Checkout blocked, with the unavailable dishes named and removable
- [x] US-22 Checkout form for name, address and contact email
- [x] US-23 Redirect to Stripe and back
- [x] US-24 Placed orders remembered in this browser, with a link back to each
- [x] US-25 Order tracking, polled every five seconds until the order finishes
- [x] US-26 Price range evolution chart on the restaurant page
- [x] US-32 Admin screen for adjusting the price range criteria
- [x] Owner area: create a restaurant, manage the menu, drafts, scheduling, stock, orders
- [x] OAuth2 sign-in through Keycloak, with route guards for the owner and admin areas
- [x] Light and dark colour schemes with a toggle, following the system setting by default
- [x] React Query throughout for server state, React Hook Form with Zod for forms
- [x] Design tokens in `theme/colors.ts`, with component defaults set once in the theme

---

## ❌ Unfinished / Planned Features

- [ ] **Component tests.** There are none. The basket reducer and `useBasketAvailability` are the two pieces where a bug would be least visible and a test most worthwhile.