# User stories

All 32 are implemented. Each row names where to look: a backend entry point, a frontend screen,
or both. Stories 13 to 21 are largely frontend work sitting on catalogue queries.

Backend paths are relative to `src/main/java/be/kdg/keepdishesgoing/`, frontend paths to the
frontend repo's `src/`.

---

## Owner

| # | Story | Where it lives | Done |
|---|---|---|---|
| 1 | Sign up / sign in to reach my management area | Keycloak realm `keepdishesgoing`, `security/SecurityConfig`; `context/SecurityContextProvider.tsx`, `components/auth/RequireOwner.tsx` | Yes |
| 2 | Create my restaurant with name, address, email, pictures, prep time, cuisine, opening hours | `CreateRestaurantUseCase`, `POST /api/restaurants`; `components/owner/RestaurantForm/` | Yes |
| 3 | Edit a dish as a draft without touching the live menu | `EditDishAsDraftUseCase`, `PUT /api/{id}/dishes/{dishId}/draft`; `pages/owner/EditDishPage.tsx` | Yes |
| 4 | Publish a dish so customers can order it | `PublishAndUnpublishDishUseCase`; `components/owner/DishActions.tsx` | Yes |
| 5 | Unpublish a dish | `PublishAndUnpublishDishUseCase` | Yes |
| 6 | Apply all pending dish changes in one action | `ApplyPendingChangesUseCase`, `POST /api/{id}/dishes/apply-changes`; `components/owner/PendingChangesActions.tsx` | Yes |
| 7 | Schedule a set of publishes to go live together | `ScheduleChangesUseCase` + `ScheduledDishPublisher` (every 60s) | Yes |
| 8 | Mark a dish out of stock or back in stock | `MarkDishInAndOutOfStockUseCase` | Yes |
| 9 | Set opening hours and manually open or close | `ChangeRestaurantStatusUseCase`, `PATCH`/`DELETE /api/restaurants/{id}/status`; `components/owner/RestaurantStatusToggle.tsx` | Yes |
| 10 | Accept or reject orders, with a reason on rejection | `AcceptOrderUseCase`, `RejectOrderUseCase`; `components/owner/OrderCard.tsx` | Yes |
| 11 | Undecided orders auto-declined after five minutes | `RejectStaleOrdersUseCase` + `OrderAutoRejectionScheduler` (every 60s) | Yes |
| 12 | Mark an accepted order ready for pickup | `MarkOrderReadyUseCase` | Yes |

## Customer

| # | Story | Where it lives | Done |
|---|---|---|---|
| 13 | Landing page lets me continue as a customer | `pages/LandingPage.tsx` | Yes |
| 14 | Explore restaurants in a list or on a map | `GET /api/restaurants`; `pages/RestaurantListPage.tsx`, `components/catalogue/RestaurantMap.tsx` (Leaflet + OpenStreetMap) | Yes |
| 15 | View a restaurant's details and dishes | `GET /api/restaurants/{id}` and `/dishes`; `pages/RestaurantDetailPage.tsx` | Yes |
| 16 | Filter restaurants by cuisine, price range, distance, delivery time | `BrowseRestaurantsQuery`; `components/catalogue/RestaurantFilterForm.tsx` | Yes |
| 17 | Filter dishes by type and food tags | `ViewRestaurantMenuQuery`; `pages/RestaurantDetailPage.tsx` | Yes |
| 18 | Sort dishes sensibly, for example by price | `DishSortOrder` on the menu query | Yes |
| 19 | See a guesstimated delivery time from location, prep time and busyness | `BrowseRestaurantsQuery`, using `OrderStatus.isVisibleToRestaurant()` for busyness; `hooks/useCustomerLocation.ts` | Yes |
| 20 | Build a basket from one restaurant's published dishes | `context/BasketContextProvider.tsx`, kept in localStorage | Yes |
| 21 | Checkout blocked if a basket dish goes out of stock or is unpublished | `GET /api/restaurants/{id}/dishes/availability`; `hooks/useBasketAvailability.ts`, enforced again server side in `PlaceOrderUseCaseImpl` | Yes |
| 22 | Give my name, delivery address and contact email at checkout | `PlaceOrderUseCase`, `POST /api/orders`; `pages/CheckoutPage.tsx` | Yes |
| 23 | Pay with the payment provider during checkout | `StripePaymentAdapter`, `ConfirmPaymentUseCase`, `StripeWebhookController`, `AbandonedCheckoutScheduler` | Yes |
| 24 | A confirmation with a link so I can return and track | `GET /api/orders/{orderId}`; `hooks/usePlacedOrders.ts`, `pages/MyOrdersPage.tsx` | Yes |
| 25 | Track my order as its status changes | `TrackOrderQuery`; `pages/OrderTrackingPage.tsx`, polled | Yes |

## KDG (the platform)

| # | Story | Where it lives | Done |
|---|---|---|---|
| 26 | See a restaurant's price range evolution | `pricing` context, `GET /api/restaurants/{id}/price-range-evolution`; `components/catalogue/PriceRangeEvolutionChart.tsx` | Yes |
| 27 | No more than 10 dishes available to customers at any moment | `RestaurantMenu.MAX_PUBLISHED_DISHES` | Yes |
| 28 | Publish messages for the delivery service on accepted and ready | `@Externalized` on `OrderAcceptedEvent` and `OrderReadyForPickupEvent` | Yes |
| 29 | Consume delivery messages for picked up, delivered and courier location | `DeliveryEventListener` on queue `keepdishesgoing.delivery-updates` | Yes |
| 30 | Customers can order without signing up or signing in | Order routes are `permitAll` in `SecurityConfig`; no customer account exists anywhere | Yes |
| 31 | Each owner manages exactly one restaurant | `OwnerId` on `Restaurant`, enforced by `CreateRestaurantUseCaseImpl` and `GET /api/restaurants/me` | Yes |
| 32 | Adjust the price range criteria | `PriceRangeCriteria` (event sourced), `POST /api/price-range-criteria`; `pages/admin/PriceRangeCriteriaPage.tsx` | Yes |

---

## Notes on the harder ones

**US-21** is checked twice on purpose. The basket asks
`GET /api/restaurants/{id}/dishes/availability` while the customer browses so the block is
visible immediately, and `PlaceOrderUseCaseImpl` checks again at checkout. The second one
catches a dish that sold out between the two, and it names the offending dishes rather than
failing with a bare message.

**US-23** needs more than a redirect. `ConfirmPaymentUseCaseImpl` treats the Stripe callback as
a hint and asks Stripe directly whether the payment is really held before letting the order
through, so a forged callback changes nothing. `AbandonedCheckoutScheduler` sweeps checkouts
nobody paid for every five minutes.

**US-26 and US-32** are the flagship pair. A restaurant's price band is never stored: it is
computed on every read by merging the restaurant's recorded menu averages with the platform's
criteria history. Because the admin can date a threshold change into the past, that merge is
what lets one edit redraw every restaurant's graph at once. See
[`domain-model.md`](domain-model.md).

**US-27** is enforced by `RestaurantMenu`, not by `Dish`. A dish cannot count its siblings.

**US-19** counts busyness from orders the restaurant has actually been told about, which is why
`OrderStatus.isVisibleToRestaurant()` exists: an unpaid order must not make a kitchen look busy.
