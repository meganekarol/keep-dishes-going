# Event catalog

Every event in the system lives in `common/events`, the shared kernel. Contexts never import
each other's classes, so an event carries plain JDK types only.

**Two kinds.** Most events are *internal*: Spring's `ApplicationEventPublisher` delivers them
in-process, inside the publisher's transaction. Two are *externalised* to RabbitMQ with Spring
Modulith's `@Externalized`, because the delivery service is another company's system.

Every event carries an `eventId` and the moment it happened, so this table lists only the
fields beyond those two.

---

## Restaurant context

Published by the `Restaurant` and `Dish` aggregates. All internal.

| Event | Payload beyond id + timestamp | Subscribed by |
|---|---|---|
| `RestaurantCreatedEvent` | `restaurantId`, `ownerId`, `name`, `cuisineType`, `defaultPreparationTime`, `street`, `number`, `postalCode`, `city`, `latitude`, `longitude`, `openingHours` | `RestaurantProjectionListener` (order) |
| `RestaurantManuallyOpenedEvent` | `restaurantId` | `RestaurantProjectionListener` (order) |
| `RestaurantManuallyClosedEvent` | `restaurantId` | `RestaurantProjectionListener` (order) |
| `RestaurantScheduleResumedEvent` | `restaurantId` | `RestaurantProjectionListener` (order) |
| `DishPublishedEvent` | `dishId`, `restaurantId`, `dishName`, `dishType`, `price`, `description` | `RestaurantProjectionListener` (order), `MenuPriceListener` (pricing) |
| `DishUnpublishedEvent` | `dishId`, `restaurantId` | `RestaurantProjectionListener` (order), `MenuPriceListener` (pricing) |
| `DishMarkedOutOfStockEvent` | `dishId`, `restaurantId`, `dishName` | `RestaurantProjectionListener` (order) |
| `DishMarkedInStockEvent` | `dishId`, `restaurantId` | `RestaurantProjectionListener` (order) |

`DishPublishedEvent` carries the price because Pricing needs it to compute a menu average, and
reaching into the Restaurant context's tables to fetch it would couple the two modules.

---

## Order context

The Order aggregate is event sourced, so these events are both its notification mechanism and
its stored state. Replaying them rebuilds the order.

| Event | Payload beyond id + timestamp | Transport | Subscribed by |
|---|---|---|---|
| `OrderPlacedEvent` | `orderId`, `restaurantId`, `customerName`, `contactEmail`, full delivery address, geocoded coordinates, line items | internal | `OrderSummaryProjectionListener`, `OrderActivityListener` (restaurant) |
| `OrderPaymentAuthorizedEvent` | `orderId`, `restaurantId`, `paymentReference`, `amount` | internal | `OrderSummaryProjectionListener` |
| `OrderPaymentCapturedEvent` | `orderId`, `restaurantId`, `paymentReference`, `amount` | internal | — |
| `OrderPaymentReleasedEvent` | `orderId`, `restaurantId`, `paymentReference` | internal | — |
| `OrderPaymentExpiredEvent` | `orderId`, `restaurantId` | internal | `OrderSummaryProjectionListener` |
| `OrderAcceptedEvent` | `restaurantId`, `orderId`, `pickupAddress`, `pickupCoordinates`, `dropoffAddress`, `dropoffCoordinates` | **RabbitMQ** | `OrderSummaryProjectionListener`, delivery service |
| `OrderRejectedEvent` | `orderId`, `restaurantId`, `rejectionReason` | internal | `OrderSummaryProjectionListener`, `OrderActivityListener` (restaurant) |
| `OrderReadyForPickupEvent` | `restaurantId`, `orderId` | **RabbitMQ** | `OrderSummaryProjectionListener`, `OrderActivityListener` (restaurant), delivery service |
| `OrderPickedUpEvent` | `restaurantId`, `orderId` | consumed from RabbitMQ | `OrderSummaryProjectionListener` |
| `OrderDeliveredEvent` | `restaurantId`, `orderId` | consumed from RabbitMQ | `OrderSummaryProjectionListener` |
| `OrderLocationEvent` | `restaurantId`, `orderId`, `lat`, `lng`, `location` | consumed from RabbitMQ | — (read by the tracking query) |

`OrderAcceptedEvent` carries both addresses in full. The delivery service is another company's
system and cannot call back to ask, so the event has to be self-contained.

`OrderLocationEvent` accepts the courier's position in two shapes, flat `lat`/`lng` and a
nested `location` point, because the specification describes one and the delivery service sends
the other.

---

## Pricing context

| Event | Payload beyond id + timestamp | Transport | Subscribed by |
|---|---|---|---|
| `PriceRangeCriteriaAdjustedEvent` | `effectiveFrom`, `cheapUnder`, `regularUnder`, `expensiveUnder` | internal, and stored | — (replayed by the aggregate) |

The only event whose subscriber is the aggregate that wrote it. `PriceRangeCriteria` is event
sourced precisely so the admin can date a change into the past: replay sorts every version by
`effectiveFrom`, which drops a back-dated one into place and rewrites every answer that
depended on it (US-32).

Note the two timestamps. `occurredOn` is when the admin clicked; `effectiveFrom` is when the
thresholds start to count and may be earlier. Confusing them is the classic event-sourcing
mistake.

---

## RabbitMQ topology

One topic exchange, `kdg.events`, declared in `common/config/RabbitMQToplogy.java`.

| Direction | Routing key | Event |
|---|---|---|
| Publish | `restaurant.<restaurantId>.order.accepted.v1` | `OrderAcceptedEvent` |
| Publish | `restaurant.<restaurantId>.order.ready.v1` | `OrderReadyForPickupEvent` |
| Consume | `delivery.*.order.pickedup.v1` | `OrderPickedUpEvent` |
| Consume | `delivery.*.order.delivered.v1` | `OrderDeliveredEvent` |
| Consume | `delivery.*.order.location.v1` | `OrderLocationEvent` |

Outbound keys are built per restaurant by a SpEL expression on the `@Externalized` annotation,
so a subscriber can bind to one restaurant or to all of them. Inbound messages all arrive on
the queue `keepdishesgoing.delivery-updates` and are dispatched on their routing key by
`DeliveryEventListener`.

The `.v1` suffix is there so a later payload change can ship as `.v2` without breaking a
consumer that is still reading the old shape.

---

## Shared payloads

`OpeningHoursSlot` sits in `common/events` but is not an event. It is one day's opening window
as it travels between contexts, carrying plain JDK types so the Order context can reason about
opening hours without depending on the Restaurant context's `OpeningHours` value object.
