# Command catalog

Every write goes through a command. A command is a record in `port/in/commands`, handled by a
use case in `port/in`, implemented in `core`, and it changes exactly one aggregate.

Queries are not listed here. They live in `port/in` alongside the use cases, return read models
from `port/in/queries`, and never change anything.

**Not every command arrives over HTTP.** Four are raised by a scheduler and three by an
incoming message or callback, which is why those rows have no endpoint.

---

## Restaurant context

Target aggregate: `Restaurant`.

| Command | Use case | Endpoint | Story |
|---|---|---|---|
| `CreateRestaurantCommand` | `CreateRestaurantUseCase` | `POST /api/restaurants` | US-02 |
| `ChangeRestaurantStatusCommand` | `ChangeRestaurantStatusUseCase` | `PATCH` and `DELETE /api/restaurants/{restaurantId}/status` | US-09 |

`PATCH` sets a manual override, `DELETE` removes it and hands the doors back to the opening
hours. Both carry the same command.

Target aggregate: `Dish`, through the `RestaurantMenu` aggregate where a rule spans the menu.

| Command | Use case | Endpoint | Story |
|---|---|---|---|
| `CreateDishCommand` | `CreateDishUseCase` | `POST /api/{restaurantId}/dishes/new` | US-03 |
| `EditDishAsDraftCommand` | `EditDishAsDraftUseCase` | `PUT /api/{restaurantId}/dishes/{dishId}/draft` | US-03 |
| `PublishDishCommand` | `PublishAndUnpublishDishUseCase` | `POST /api/{restaurantId}/dishes/{dishId}/publish` | US-04 |
| `UnpublishDishCommand` | `PublishAndUnpublishDishUseCase` | `POST /api/{restaurantId}/dishes/{dishId}/unpublish` | US-05 |
| `MarkDishOutOfStockCommand` | `MarkDishInAndOutOfStockUseCase` | `POST /api/{restaurantId}/dishes/{dishId}/mark-out-of-stock` | US-08 |
| `MarkDishInStockCommand` | `MarkDishInAndOutOfStockUseCase` | `POST /api/{restaurantId}/dishes/{dishId}/mark-in-stock` | US-08 |
| `ScheduleDishChangesCommand` | `ScheduleChangesUseCase` | `POST /api/{restaurantId}/dishes/schedule-changes` | US-07 |
| `ApplyPendingChangesCommand` | `ApplyPendingChangesUseCase` | `POST /api/{restaurantId}/dishes/apply-changes` | US-06 |
| `PublishScheduledChangesCommand` | `PublishScheduledChangesUseCase` | *(scheduler, every 60s)* | US-07 |

Publishing goes through `RestaurantMenu` rather than straight to `Dish`, because the ten-live-
dishes cap of US-27 is a rule about the menu as a whole. A single dish cannot enforce it.

`PublishScheduledChangesCommand` is raised by `ScheduledDishPublisher`, which is what makes a
booked change go live without anyone pressing anything.

---

## Order context

Target aggregate: `Order`. Event sourced and snapshotted.

| Command | Use case | Endpoint | Story |
|---|---|---|---|
| `PlaceOrderCommand` | `PlaceOrderUseCase` | `POST /api/orders` | US-22, US-30 |
| `ConfirmPaymentCommand` | `ConfirmPaymentUseCase` | *(Stripe webhook)* | US-23 |
| `AcceptOrderCommand` | `AcceptOrderUseCase` | `POST /api/restaurants/{restaurantId}/orders/{orderId}/accept` | US-10 |
| `RejectOrderCommand` | `RejectOrderUseCase` | `POST /api/restaurants/{restaurantId}/orders/{orderId}/reject` | US-10 |
| `MarkOrderReadyCommand` | `MarkOrderReadyUseCase` | `POST /api/restaurants/{restaurantId}/orders/{orderId}/ready` | US-12 |
| `RejectStaleOrdersCommand` | `RejectStaleOrdersUseCase` | *(scheduler, every 60s)* | US-11 |
| `ExpireAbandonedCheckoutsCommand` | `ExpireAbandonedCheckoutsUseCase` | *(scheduler, every 5 min)* | US-23 |
| `UpdateOrderStatusCommand` | `UpdateOrderStatusUseCase` | *(RabbitMQ, delivery service)* | US-29 |
| `RecordCourierLocationCommand` | `RecordCourierLocationUseCase` | *(RabbitMQ, delivery service)* | US-29 |

`PlaceOrderCommand` contains a list of `OrderItemCommand`, one per basket line. That is a nested
value inside the command, not a command in its own right.

`ConfirmPaymentCommand` arrives from `StripeWebhookController`, but the callback is treated as a
hint rather than proof: `ConfirmPaymentUseCaseImpl` asks Stripe directly whether the payment is
really held before letting the order through to its restaurant. A forged callback with a valid
signature but a made-up payment reference changes nothing.

`RejectStaleOrdersCommand` is what enforces the five-minute decision window of US-11. The
scheduler sweeps rather than each order carrying a timer, so a restart cannot lose one.

---

## Pricing context

Target aggregate: `PriceRangeCriteria`. Event sourced, one instance for the whole platform.

| Command | Use case | Endpoint | Story |
|---|---|---|---|
| `AdjustPriceRangeCriteriaCommand` | `AdjustPriceRangeCriteriaUseCase` | `POST /api/price-range-criteria` | US-32 |

`POST`, never `PUT`. It appends a version to a history rather than replacing the thresholds, and
older versions keep governing their own periods. Its `effectiveFrom` may be in the past, which
is the whole point of US-32.

The Pricing context has no command for recording a restaurant's menu average. That happens in
`MenuPriceListener` in reaction to a dish event, not because anyone asked for it.

---

## Authorisation

| Route pattern | Required |
|---|---|
| `GET /api/restaurants`, `/{id}`, `/{id}/dishes`, `/{id}/dishes/availability` | open |
| `GET /api/restaurants/{id}/price-range-evolution` | open |
| `POST /api/orders`, `GET /api/orders/{orderId}` | open |
| `POST /api/payments/stripe/webhook` | open, guarded by an HMAC signature |
| `POST /api/restaurants`, `/{id}/status`, `/{id}/orders/**`, `/api/*/dishes/**` | role `owner` |
| `/api/price-range-criteria`, `POST /api/pricing/demo-history` | role `admin` |

Customers are never asked to sign in (US-30), so every customer-facing route is open. The owner
routes additionally carry `@PreAuthorize("@restaurantOwnership.isOwner(...)")`: the role proves
the caller is *an* owner, that check proves they own *this* restaurant.
