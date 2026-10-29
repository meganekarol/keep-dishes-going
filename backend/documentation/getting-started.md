# Getting started

From a clean machine to a running Keep Dishes Going with demo data.

## Quick start: everything in Docker

All you need is Docker Desktop. From the repository root:

```bash
cd backend/infrastructure
docker compose --profile full up -d --build
```

The first run builds three images and takes several minutes. **Keycloak needs about two more
minutes on its first boot** while it sets up its database; until then signing in fails. It is ready
when http://localhost:8180/realms/keepdishesgoing answers.

Then open **http://localhost:5173**:

| As | Sign in with | Try |
|---|---|---|
| Customer | nothing, guests order without an account | Browse the list or map, add dishes to a basket, check out |
| Owner | `owner` / `owner` | Manage La Petite Maison: dishes, opening hours, incoming orders |
| Admin | `kdg-admin` / `kdg-admin` | Price range criteria, and **Load demo history** for the price graph |

What is already there:

- **Four demo restaurants** with published menus, created on first start by
  `DemoRestaurantSeeder`. The first one belongs to `owner`. Turn it off with `SEED_DEMO_DATA=false`.
- **Simulated payments.** Without a Stripe key, checkout opens a simulated payment page instead of
  Stripe's. Pressing **Pay** goes through the same confirmation use case as a real Stripe callback.
  To use Stripe instead, see section 4.
- **A simulated courier.** Once the owner accepts an order and marks it ready, the delivery
  simulator picks it up, reports positions along the route and delivers it within about 40 seconds.

A full round trip: order as a guest, pay, sign in as `owner`, accept the order, mark it ready, then
watch the tracking page until it says delivered.

Stop it with `docker compose --profile full down`. Data is kept in Docker volumes, so the next
`up` picks up where you left off.

The rest of this guide runs the backend and frontend outside Docker, for development.

---

## 1. What you need installed

| Tool | Version | Check with |
|---|---|---|
| JDK | 17 (Gradle downloads it if missing) | `java -version` |
| Docker Desktop | any current | `docker compose version` |
| Node.js | 20 or newer | `node -v` |
| Stripe CLI | any current, optional | `stripe --version` |

The Stripe CLI is only needed to pay through Stripe's real test checkout (US-23). Without a Stripe
key the backend uses simulated payments instead, as in the quick start.

---

## 2. Start the infrastructure

```bash
cd backend/infrastructure
docker compose up -d
```

Five containers come up: Postgres, RabbitMQ, MySQL, Keycloak, and the delivery simulator. The
simulator is built locally from `infrastructure/delivery-simulator/` on the first run. It plays the
external courier company: once an owner marks an order ready, it reports the order picked up after
about 10 seconds, sends a courier position every 3 seconds, and delivers it about 30 seconds later.

**The first boot takes a few minutes.** MySQL initialises its data directory, and Keycloak waits
on a healthcheck before starting, so it will look stuck for a while. That is expected.

Check it worked:

```bash
docker compose ps
curl -o /dev/null -w "%{http_code}\n" http://localhost:8180/realms/keepdishesgoing   # expect 200
```

A 200 from that last command means Keycloak came up **and** imported the realm.

| Service | URL / port | Credentials |
|---|---|---|
| PostgreSQL (application) | `localhost:5443`, db `keep-dishes-going` | `user` / `password` |
| RabbitMQ (AMQP) | `localhost:5672` | `user` / `password` |
| RabbitMQ (management UI) | http://localhost:15672 | `user` / `password` |
| Keycloak (admin console) | http://localhost:8180 | `admin` / `admin` |
| MySQL (Keycloak's store) | `localhost:3307` | `keycloak` / `password` |
| Delivery simulator | no port, RabbitMQ only | — |

---

## 3. The Keycloak realm

**Nothing to do.** `infrastructure/keycloak/keepdishesgoing-realm.json` is version controlled and
imported automatically by `start-dev --import-realm`. It brings two accounts with it:

| Account | Password | Realm roles |
|---|---|---|
| `owner` | `owner` | `owner` |
| `kdg-admin` | `kdg-admin` | `admin` |

Two clients are configured: `backend-client` (confidential, used by `.http` files to fetch a
token) and `frontend-client` (public, PKCE, used by the React app).

> **`--import-realm` only imports when the realm does not already exist.** If you change
> something in the console and want it version controlled, re-export and commit:
>
> ```bash
> docker compose exec idp_keycloak /opt/keycloak/bin/kc.sh export \
>   --dir /opt/keycloak/data/import --realm keepdishesgoing --users realm_file
> ```
>
> **Strip the secrets before committing.** An export contains the realm's signing keys (the
> `org.keycloak.keys.KeyProvider` entry under `components`) and `backend-client`'s `secret`.
> Delete both. Keycloak generates fresh ones on import, and the `.http` files fetch the client
> secret through the admin API, so nothing depends on the exported values.

**`kdg-admin` is deliberately not an owner.** Every account in the realm gets the `owner` role by
default, which is how an owner can self-register from the landing page. KDG's own account has
that default mapping removed so it holds `admin` alone. If you rebuild the realm from the console
rather than the export, redo that or the admin will see a "My restaurant" button that 403s.

---

## 4. Stripe credentials (optional)

**Skip this section to use simulated payments.** With no Stripe key configured, the backend logs
`payments are SIMULATED` at startup and checkout opens a simulated payment page served by the
backend. Everything after payment (the restaurant accepting, capturing, releasing) runs exactly as
with Stripe.

To use Stripe's real test checkout instead: payment keys are **not** in the repository. Spring Boot reads them from `backend/config/`, which
git ignores.

Copy `backend/config/application.yml.example` to `backend/config/application.yml` and fill it in:

```yaml
kdg:
  payment:
    stripe:
      secret-key: sk_test_...
      webhook-secret: whsec_...
```

`sk_test_...` comes from the Stripe dashboard in **test mode**. `whsec_...` comes from the
listener you start in section 6 — run that first if you do not have one yet.

Setting `secret-key` is what switches the backend from simulated payments to Stripe. Without
`webhook-secret`, `StripeSignatureVerifier` refuses every callback, so orders never get past payment.

The Docker quick start reads the same file, which is mounted into the backend container. It also
accepts `STRIPE_SECRET_KEY` and `STRIPE_WEBHOOK_SECRET` from `infrastructure/.env`.

---

## 5. Run the backend

```bash
cd backend
./gradlew bootRun
```

It listens on http://localhost:8080. Schemas are created automatically: `ddl-auto` is `update`
with `create_namespaces: true`, so `kdg_restaurant`, `kdg_order` and `kdg_pricing` appear on
first boot.

Because it is `update` and not `create-drop`, **your data survives restarts.** You set the demo
up once.

---

## 6. Forward Stripe webhooks (only with Stripe)

Skip this with simulated payments. Stripe cannot reach `localhost`, so a payment made in the browser will never come back on its own.
The CLI bridges that:

```bash
stripe login
stripe listen --events checkout.session.completed,checkout.session.expired --forward-to localhost:8080/api/payments/stripe/webhook
```

Those two events are the only ones the backend handles. Newer CLI versions refuse to start without
`--events`.

It prints a `whsec_...` on startup. That is the value for `webhook-secret` in section 4. **Leave
this running** for the whole session, and restart the backend if you had to add the secret.

Skip this and orders stay at `AWAITING_PAYMENT` forever. They will not appear in the owner's list,
which is correct behaviour, not a bug.

---

## 7. Run the frontend

```bash
cd frontend
npm install
npm run dev          # http://localhost:5173
```

`.env` is committed on purpose: it holds addresses, not secrets, since the Keycloak client is
public. Put machine-specific overrides in `.env.local`, which git ignores.

**Stay on port 5173.** The backend allows only `http://localhost:5173` through CORS, and the
Keycloak client's redirect URIs say the same.

---

## 8. Demo data

**Restaurants are seeded for you.** On startup `DemoRestaurantSeeder` creates four restaurants in
Antwerp with published menus, through the same use cases the owner's screens use. The first, La
Petite Maison, belongs to the `owner` account. Each restaurant is only created when its owner has
none yet, so restarts add nothing. Set `SEED_DEMO_DATA=false` (or `kdg.demo.seed-data: false`)
to start from an empty catalogue.

**Ordering.** Build a basket in the browser and check out:

- With simulated payments, press **Pay** on the simulated checkout page.
- With Stripe, pay with test card `4242 4242 4242 4242`, any future expiry and any CVC. The
  webhook arrives through the CLI from section 6.

Either way the order then appears in the owner's list. `ConfirmPaymentUseCaseImpl` asks the
payment provider whether the money is genuinely held before letting an order through, so a forged
callback cannot get an order past payment.

**Exercising every feature through the API.** `src/test/http/test.http` registers a second owner,
builds a menu that covers drafts, scheduling and stock, places a guest order and loads price
history. Run steps 1 to 43 in order from IntelliJ. Set `@kdgAdminPass` at the top first if you
changed that account's password.

**For the price range graph.** Sign in as `kdg-admin`, open **Price range criteria**, and press
**Load demo history**. It writes a year of back-dated thresholds and menu averages so the graph
has a shape. Pressing it twice adds nothing.

---

## 9. Resetting

```bash
cd backend/infrastructure
docker compose --profile full down -v
docker compose up -d        # or --profile full up -d for the Docker quick start
```

`-v` deletes the Docker volumes. This wipes the application database **and** Keycloak, so the realm is re-imported from the
committed export, and the two accounts in section 3 come back with their passwords. Any account
you created yourself is gone.

---

## Troubleshooting

**Signing in fails, or the login page does not load, right after the first start.** Keycloak
takes about two minutes on its first boot while it creates its database. Wait until
`curl http://localhost:8180/realms/keepdishesgoing` answers 200.

**Keycloak exits immediately on first boot.** It raced MySQL. `docker compose up -d idp_keycloak`
once MySQL is healthy.

**Every secured endpoint returns 401.** Check the realm is really there:
`curl http://localhost:8180/realms/keepdishesgoing`. The `issuer-uri` in `application.yml` must
match exactly, including the realm name.

**Owner endpoints return 403 with a valid token.** The account is missing the `owner` role. Check
under Users → the account → Role mapping.

**The admin screen 403s.** The account needs the `admin` realm role, which only `kdg-admin` has by
default.

**Checkout fails to open a payment.** With Stripe, the `secret-key` in
`backend/config/application.yml` is wrong or not in test mode. See section 4.

**Checkout opens the simulated page but you wanted Stripe.** No `secret-key` reached the backend.
Check `backend/config/application.yml`, and restart the backend after changing it.

**Payment succeeds but the order never reaches the restaurant.** `stripe listen` is not running,
or `webhook-secret` does not match the one it printed. The backend logs
`Refusing a Stripe callback whose signature does not match`.

**The frontend loads but every request fails with a CORS error.** The dev server is not on 5173.

**The price graph is one flat point.** No history has been recorded yet. Press **Load demo
history** on the admin screen.
