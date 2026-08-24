# Keep Dishes Going, Backend

Spring Boot 3.5 / Java 17 / Gradle. Three bounded contexts (`restaurant`, `order`, `pricing`)
built with hexagonal architecture and Spring Modulith.

- **Setting up from scratch: [`documentation/getting-started.md`](documentation/getting-started.md)**
- Domain model, command and event catalog, user stories: [`documentation/`](documentation/)
- Infrastructure (Docker Compose): [`infrastructure/`](infrastructure/)

## Challenges & Accomplishments

### Challenges

**1. Sticking to the DDD and hexagonal architecture rules.**
This was the thing I had to get used to the most. Even a small feature meant writing two or
three extra classes: a command, a port, an adapter, and then the use case itself. At the start
it felt slow and I was often tempted to just call the repository straight from the controller.
It did pay off later. By the time I added the pricing feature I already knew where every piece
had to go.

**2. Integrating the delivery service.**
Messages were going out over RabbitMQ but the answers coming back were not doing what I
expected. In the end it was a bug in one of the events. It took me a long time to find because
nothing crashed and no error showed up, the message just quietly did nothing. Once I found it
the rest of the integration was easy.

**3. Understanding and implementing the price range criteria.**
This one took the longest just to understand. The hard part is that the admin can change what
"cheap" means, and can set that change in the past. So I could not simply save a restaurant's
price range in the database, because one change would make every restaurant's history wrong.
What I do instead is store nothing and work it out again on every read, by combining the
restaurant's own price history with the history of the criteria. That is also the reason this
part is event sourced.

### What I am proud of

The rules live in the domain classes instead of in services. `RestaurantMenu` is the class that
checks the limit of ten dishes, because a single `Dish` cannot count the other dishes.
`OrderStatus` decides for itself which step is allowed to follow which.

The three contexts also never call each other directly. They only talk through events. The
pricing context needs dish prices, but instead of reading the restaurant tables it builds its
own small copy from the dish events it listens to.

---

## ✅ Finished Features

All 32 user stories are implemented. See
[`documentation/user-stories.md`](documentation/user-stories.md) for where each one lives.

**Owner**
- [x] US-01 Sign up and sign in through Keycloak
- [x] US-02 Create a restaurant with address, contact, pictures, prep time, cuisine and opening hours
- [x] US-03 Edit a dish as a draft without touching the live menu
- [x] US-04 Publish a dish
- [x] US-05 Unpublish a dish
- [x] US-06 Apply all pending dish changes in one action
- [x] US-07 Schedule publishes to go live together at a chosen time
- [x] US-08 Mark a dish out of stock or back in stock
- [x] US-09 Set opening hours and manually open or close
- [x] US-10 Accept or reject orders, with a reason on rejection
- [x] US-11 Undecided orders declined automatically after five minutes
- [x] US-12 Mark an accepted order ready for pickup

**Customer**
- [x] US-13 Landing page offering the customer route
- [x] US-14 Explore restaurants in a list or on a map
- [x] US-15 View a restaurant's details and dishes
- [x] US-16 Filter restaurants by cuisine, price range, distance and delivery time
- [x] US-17 Filter dishes by course and food tags
- [x] US-18 Sort dishes, for example by price
- [x] US-19 Guesstimated delivery time from location, prep time and busyness
- [x] US-20 Build a basket from a single restaurant
- [x] US-21 Checkout blocked when a basket dish sells out or is unpublished
- [x] US-22 Give name, delivery address and contact email at checkout
- [x] US-23 Pay through Stripe, with abandoned checkouts swept up
- [x] US-24 Confirmation with a link back to the order
- [x] US-25 Track an order as its status changes

**KDG (the platform)**
- [x] US-26 Price range evolution graph per restaurant
- [x] US-27 No more than 10 dishes available at any moment
- [x] US-28 Publish messages to the delivery service on accepted and ready
- [x] US-29 Consume delivery messages for picked up, delivered and courier location
- [x] US-30 Customers order without signing up
- [x] US-31 Each owner manages exactly one restaurant
- [x] US-32 Adjust the price range criteria, including retroactively

**Technical requirements**
- [x] Hexagonal architecture with ports and adapters throughout
- [x] Three bounded contexts as Spring Modulith modules, communicating only through events
- [x] Event sourcing on the `Order` aggregate, with snapshots every 50 events
- [x] Event sourcing on `PriceRangeCriteria`, which is what makes retroactive edits possible
- [x] Projected read models per context
- [x] RabbitMQ integration, publishing and consuming, over a topic exchange
- [x] OAuth2 with Keycloak, realm roles `owner` and `admin`, per-restaurant ownership checks
- [x] Documentation folder with domain model, command catalog and event catalog

---

## ❌ Unfinished / Planned Features

Every user story is done. What is missing is engineering polish rather than functionality.

- [ ] **Automated tests.** Only `KeepDishesGoingApplicationTests.contextLoads` exists.
- [ ] **Bean validation on request DTOs.** Validation currently lives in the domain constructors,
      so bad input fails deeper in the call than it needs to.
