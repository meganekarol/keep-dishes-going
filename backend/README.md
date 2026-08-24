# Keep Dishes Going — Backend

Spring Boot 3.5 / Java 17 / Gradle. Three bounded contexts (`restaurant`, `order`, `pricing`) built with
hexagonal architecture and Spring Modulith. `order` is event sourced with snapshots, `pricing` is
event sourced without them.

- **Setting up from scratch: [`documentation/getting-started.md`](documentation/getting-started.md)**
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

The realm `keepdishesgoing` is version controlled at
`infrastructure/keycloak/keepdishesgoing-realm.json` and imported automatically on a clean start,
bringing two accounts and both clients with it.

See [`documentation/getting-started.md`](documentation/getting-started.md) for what it contains,
how to re-export it after console changes, and why `kdg-admin` deliberately has the default role
mapping removed.

## Payment credentials

Stripe keys live in `config/application.yml`, which git ignores. Section 4 of the getting started
guide has the shape, and section 6 covers forwarding webhooks with the Stripe CLI.

## Resetting

```bash
cd infrastructure
docker compose down -v          # also removes containers + networks
rm -rf postgresql rabbitmq idp_mysql_data
docker compose up -d
```

The realm is restored automatically from the committed export.
