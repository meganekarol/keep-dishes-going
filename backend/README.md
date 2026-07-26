# Keep Dishes Going — Backend

Spring Boot 3.5 / Java 17 / Gradle. Two bounded contexts (`restaurant`, `order`) built with
hexagonal architecture and Spring Modulith. The `order` context is event sourced with snapshots.

- Domain model, command & event catalog: [`documentation/`](documentation/)
- Infrastructure (Docker Compose): [`infrastructure/`](infrastructure/)

## Prerequisites

- JDK 17
- Docker Desktop

## Running

Start the infrastructure first — the application will not boot without Postgres, and will not
authenticate without Keycloak.

```bash
cd infrastructure
docker compose up -d
```

First boot takes a few minutes: MySQL initialises its data directory and Keycloak waits on its
healthcheck before starting. Verify everything is up:

```bash
docker compose ps
curl -o /dev/null -w "%{http_code}\n" http://localhost:8180/realms/keepdishesgoing   # expect 200
```

Then run the application:

```bash
./gradlew bootRun
```

### Services

| Service | URL / Port | Credentials |
|---|---|---|
| Application | http://localhost:8080 | — |
| PostgreSQL (app) | `localhost:5443`, db `keep-dishes-going` | `user` / `password` |
| RabbitMQ AMQP | `localhost:5672` | `user` / `password` |
| RabbitMQ management | http://localhost:15672 | `user` / `password` |
| Keycloak admin console | http://localhost:8180 | `admin` / `admin` |
| MySQL (Keycloak store) | `localhost:3307` | `keycloak` / `password` |
| Delivery service (external) | http://localhost:8095 | — |

Schemas are created automatically (`ddl-auto: update` with `create_namespaces: true`).

## Keycloak realm

The application expects a realm named **`keepdishesgoing`** — this is hardcoded in
`src/main/resources/application.yml` as the `issuer-uri`. Without it, every secured endpoint
fails token validation.

The realm is version controlled at `infrastructure/keycloak/realm-export.json` and imported
automatically on startup, so a clean `docker compose up` yields a working identity provider.

### What the realm must contain

These values are dictated by the code and cannot be changed independently:

| Item | Required value | Why |
|---|---|---|
| Realm name | `keepdishesgoing` | `application.yml` `issuer-uri` |
| Realm role | `owner` (lowercase) | `SecurityConfig` uses `.hasRole("owner")`; `KeycloakRealmRoleConverter` maps `realm_access.roles` → `ROLE_owner` |
| Client | `kdg-frontend`, public, PKCE `S256` | React SPA cannot hold a secret |
| Valid redirect URIs | `http://localhost:5173/*` | Vite dev server |
| Web origins | `http://localhost:5173` | Must match `cors.allowed-origins` |

### Recreating the realm by hand

Only needed if the export is lost. In the admin console at http://localhost:8180 (`admin`/`admin`):

1. **Create realm** → name `keepdishesgoing`.
2. **Realm roles** → *Create role* → name `owner`.
3. **Clients** → *Create client*
   - Client ID `kdg-frontend`, client type **OpenID Connect**
   - Client authentication **Off** (public), Standard flow **On**
   - Direct access grants **On** (lets you fetch a token with `curl` for `.http` testing)
   - Valid redirect URIs `http://localhost:5173/*`, Web origins `http://localhost:5173`
   - Advanced → *Proof Key for Code Exchange* → `S256`
4. **Users** → *Create user* (e.g. `owner1`) → *Credentials* tab → set a password, Temporary **Off**
   → *Role mapping* tab → *Assign role* → `owner`.

Then re-export (see below) and commit the result.

### Exporting the realm after console changes

```bash
docker compose exec idp_keycloak /opt/keycloak/bin/kc.sh export \
  --dir /opt/keycloak/data/import --realm keepdishesgoing --users realm_file
```

The file lands in `infrastructure/keycloak/` via the bind mount. Commit it.

> Note: exports do **not** include user passwords. After importing on a fresh machine, set the
> test user's password again in the console.

### Getting a token for manual API testing

```bash
curl -s -X POST http://localhost:8180/realms/keepdishesgoing/protocol/openid-connect/token \
  -d grant_type=password -d client_id=kdg-frontend \
  -d username=owner1 -d password=<password>
```

## Resetting

```bash
cd infrastructure
docker compose down -v          # also removes containers + networks
rm -rf postgresql rabbitmq idp_mysql_data
docker compose up -d
```

The realm is restored automatically from the committed export.