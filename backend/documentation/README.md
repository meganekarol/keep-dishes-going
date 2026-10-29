# Documentation

Lightweight documentation for the Keep Dishes Going backend.

| Document | Purpose | Status |
|---|---|---|
| [`domain-model.md`](domain-model.md) | Bounded contexts, aggregates, entities, value objects | Done |
| [`command-catalog.md`](command-catalog.md) | All 21 commands, their use case, aggregate and endpoint | Done |
| [`event-catalog.md`](event-catalog.md) | All 20 events, payload, publisher, subscriber, routing key | Done |
| [`user-stories.md`](user-stories.md) | The 32 user stories with implementation status | Done |
| [`getting-started.md`](getting-started.md) | Setting the project up from scratch and running it | Done |

## Context map (summary)

Three bounded contexts, each a Spring Modulith `@ApplicationModule`. They never call each
other: everything crossing a boundary is an event from the shared kernel in `common/events`.

```text
                       ┌──────────────────────┐
                       │  Restaurant context  │
                       │  (state stored, JPA) │
                       │                      │
                       │  Restaurant (AR)     │
                       │  Dish (AR)           │
                       │  RestaurantMenu      │
                       └───┬──────────────┬───┘
                           │              │
        dish events        │              │  dish events
        in-process         │              │  in-process
                           ▼              ▼
     ┌──────────────────────┐          ┌──────────────────────────┐
     │    Order context     │          │    Pricing context       │
     │   (event sourced,    │◀────────▶│   (event sourced, no     │
     │    snapshotted)      │  order   │    snapshots needed)     │
     │                      │  events  │                          │
     │  Order (AR)          │          │  PriceRangeCriteria (AR) │
     │  Dish/Restaurant     │          │  MenuDish projection     │
     │  projections         │          │  MenuPricePoint log      │
     └──────────┬───────────┘          └──────────────────────────┘
                │
                │ RabbitMQ, topic exchange kdg.events
                ▼
     ┌──────────────────────┐
     │  Delivery service    │
     │  (external, another  │
     │   company)           │
     └──────────────────────┘
```

Only the Order context talks to the outside world. Restaurant and Pricing publish and consume
in-process events only.

**Techniques used:** Shared Kernel (`common/events`), Customer/Supplier via projections
(Restaurant → Order, Restaurant → Pricing), Published Language + Conformist
(Order ↔ Delivery service).

**Why Pricing is its own context.** It speaks a different language from Restaurant: not dishes
and menus but thresholds, bands and the moments they take effect. It keeps its own copy of
dish prices (`MenuDish`) built from the events it hears, so it never reads Restaurant's tables.
See [`domain-model.md`](domain-model.md).

## Routing keys

| Direction | Routing key | Event |
|---|---|---|
| Publish | `restaurant.<restaurantId>.order.accepted.v1` | `OrderAcceptedEvent` |
| Publish | `restaurant.<restaurantId>.order.ready.v1` | `OrderReadyForPickupEvent` |
| Consume | `delivery.*.order.pickedup.v1` | `OrderPickedUpEvent` |
| Consume | `delivery.*.order.delivered.v1` | `OrderDeliveredEvent` |
| Consume | `delivery.*.order.location.v1` | `OrderLocationEvent` |