# Documentation

Lightweight documentation for the Keep Dishes Going backend.

| Document | Purpose | Status |
|---|---|---|
| [`domain-model.md`](domain-model.md) | Bounded contexts, aggregates, entities, value objects, context map | TODO |
| [`command-catalog.md`](command-catalog.md) | Every command, its handler, and the aggregate it targets | TODO |
| [`event-catalog.md`](event-catalog.md) | Every domain and integration event, payload, publisher, subscriber, routing key | TODO |
| [`user-stories.md`](user-stories.md) | The 32 user stories with implementation status | TODO |
| [`wireframes/`](wireframes/) | Frontend wireframes | TODO |

## Context map (summary)

```text
┌──────────────────────┐                      ┌──────────────────────┐
│  Restaurant context  │                      │    Order context     │
│  (state stored, JPA) │                      │   (event sourced)    │
│                      │  domain events       │                      │
│  Restaurant (AR)     │─────────────────────▶│  Dish/Restaurant     │
│  Dish (AR)           │  in-process listener │  projections         │
│  RestaurantMenu      │                      │  Order (AR)          │
└──────────────────────┘                      └──────────┬───────────┘
                                                         │
                          Published Language             │ RabbitMQ
                          exchange: kdg.events           │ topic exchange
                                                         ▼
                                              ┌──────────────────────┐
                                              │  Delivery service    │
                                              │  (external, other    │
                                              │   company)           │
                                              └──────────────────────┘
```

**Techniques used:** Shared Kernel (`common/events`), Customer/Supplier via projections
(Restaurant → Order), Published Language + Conformist (Order ↔ Delivery service).

## Routing keys

| Direction | Routing key | Event |
|---|---|---|
| Publish | `restaurant.<restaurantId>.order.accepted.v1` | `OrderAcceptedEvent` |
| Publish | `restaurant.<restaurantId>.order.ready.v1` | `OrderReadyForPickupEvent` |
| Consume | `delivery.*.order.pickedup.v1` | `OrderPickedUpEvent` |
| Consume | `delivery.*.order.delivered.v1` | `OrderDeliveredEvent` |
| Consume | `delivery.*.order.location.v1` | `OrderLocationEvent` |