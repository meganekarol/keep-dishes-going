# Getting started

From a clean machine to a running Keep Dishes Going with demo data, in about fifteen minutes.

Two repositories are involved. This guide assumes they sit side by side:

```text
programming6/
  backend/     this repository
  frontend/    the React client
```

---

## 1. What you need installed

| Tool | Version | Check with |
|---|---|---|
| JDK | 17 | `java -version` |
| Docker Desktop | any current | `docker compose version` |
| Node.js | 20 or newer | `node -v` |
| Stripe CLI | any current | `stripe --version` |

The Stripe CLI is only needed to demonstrate paying for an order (US-23). Everything else runs
without it, and unpaid orders behave correctly — they simply never reach a restaurant.

---

## 2. Start the infrastructure

```bash
cd backend/infrastructure
docker compose up -d
```

Five containers come up: Postgres, RabbitMQ, MySQL, Keycloak, and the delivery service.

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
| Delivery service | http://localhost:8095 | — |

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
> With `--file` instead of `--dir`, the flag is `--users same_file`. Both forms include user
> passwords. Re-importing needs a clean Keycloak volume, so verifying an export means resetting
> (section 8).

**`kdg-admin` is deliberately not an owner.** Every account in the realm gets the `owner` role by
default, which is how an owner can self-register from the landing page. KDG's own account has
that default mapping removed so it holds `admin` alone. If you rebuild the realm from the console
rather than the export, redo that or the admin will see a "My restaurant" button that 403s.

---

## 4. Stripe credentials

Payment keys are **not** in the repository. Spring Boot reads them from `backend/config/`, which
git ignores.

Create `backend/config/application.yml`:

```yaml
kdg:
  payment:
    stripe:
      secret-key: sk_test_...
      webhook-secret: whsec_...
```

`sk_test_...` comes from the Stripe dashboard in **test mode**. `whsec_...` comes from the
listener you start in section 6 — run that first if you do not have one yet.

Without these the application still boots. Checkout will fail when it tries to open a payment,
and `StripeSignatureVerifier` logs a warning at startup saying every callback will be refused.

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

## 6. Forward Stripe webhooks

Stripe cannot reach `localhost`, so a payment made in the browser will never come back on its own.
The CLI bridges that:

```bash
stripe login
stripe listen --forward-to localhost:8080/api/payments/stripe/webhook
```

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

## 8. Load some data

A fresh database has no restaurants, so the catalogue is empty and the price graph has nothing to
draw.

**Fastest path.** Open `src/test/http/test.http` in IntelliJ and run steps 1 to 43 in order. That
registers an owner in Keycloak, creates a restaurant, builds a five-dish menu exercising drafts,
scheduling and stock, places a guest order, and loads a year of back-dated price history.

Set `@kdgAdminPass` at the top of the file first if you changed that account's password.

**Then, for the parts that need a real payment.** Steps 39 onward stop short of the owner's order
flow, because `ConfirmPaymentUseCaseImpl` asks Stripe whether a payment is genuinely held before
letting an order through — so nothing can fake its way past. To see US-10, US-12, US-24 and
US-25:

1. Build a basket in the browser and go through checkout
2. Pay with test card `4242 4242 4242 4242`, any future expiry, any CVC
3. The webhook arrives through the CLI from section 6 and the order appears in the owner's list

**For the price range graph.** Sign in as `kdg-admin`, open **Price range criteria**, and press
**Load demo history**. It writes a year of back-dated thresholds and menu averages so the graph
has a shape. Pressing it twice adds nothing.

---

## 9. Resetting

```bash
cd backend/infrastructure
docker compose down -v
rm -rf postgresql rabbitmq idp_mysql_data
docker compose up -d
```

This wipes the application database **and** Keycloak, so the realm is re-imported from the
committed export, and the two accounts in section 3 come back with their passwords. Any account
you created yourself is gone.

---

## Troubleshooting

**Keycloak exits immediately on first boot.** It raced MySQL. `docker compose up -d idp_keycloak`
once MySQL is healthy.

**Every secured endpoint returns 401.** Check the realm is really there:
`curl http://localhost:8180/realms/keepdishesgoing`. The `issuer-uri` in `application.yml` must
match exactly, including the realm name.

**Owner endpoints return 403 with a valid token.** The account is missing the `owner` role. Check
under Users → the account → Role mapping.

**The admin screen 403s.** The account needs the `admin` realm role, which only `kdg-admin` has by
default.

**Checkout fails to open a payment.** `backend/config/application.yml` is missing or has no
`secret-key`. See section 4.

**Payment succeeds but the order never reaches the restaurant.** `stripe listen` is not running,
or `webhook-secret` does not match the one it printed. The backend logs
`Refusing a Stripe callback whose signature does not match`.

**The frontend loads but every request fails with a CORS error.** The dev server is not on 5173.

**The price graph is one flat point.** No history has been recorded yet. Press **Load demo
history** on the admin screen.
