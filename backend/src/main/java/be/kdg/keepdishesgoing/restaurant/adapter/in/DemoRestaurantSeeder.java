package be.kdg.keepdishesgoing.restaurant.adapter.in;

import be.kdg.keepdishesgoing.restaurant.domain.Dish;
import be.kdg.keepdishesgoing.restaurant.domain.Restaurant;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.Address;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.CuisineType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.DishType;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.FoodTag;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.OpeningHours;
import be.kdg.keepdishesgoing.restaurant.domain.valueObjects.TimeRange;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateDishUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.CreateRestaurantUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.GetOwnerRestaurantQuery;
import be.kdg.keepdishesgoing.restaurant.port.in.PublishAndUnpublishDishUseCase;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateDishCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.CreateRestaurantCommand;
import be.kdg.keepdishesgoing.restaurant.port.in.commands.PublishDishCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Fills an empty catalogue with a few restaurants on startup, so a fresh
 * install has something to browse and order from.
 *
 * <p>Goes through the same use cases as the owner's screens, so every rule
 * (one restaurant per owner, ten live dishes at most) applies to the demo data
 * too, and the other contexts learn about it from the usual events.
 *
 * <p>The first restaurant belongs to the realm's {@code owner} account, whose
 * id is fixed by the committed Keycloak export, so signing in as
 * {@code owner}/{@code owner} lands on a running restaurant. The others belong
 * to owners nobody can sign in as; they are there to be browsed.
 *
 * <p>Each restaurant is skipped when its owner already has one, which makes this
 * safe to run on every start. Turn it off with {@code kdg.demo.seed-data=false}.
 */
@Component
@ConditionalOnProperty(name = "kdg.demo.seed-data", havingValue = "true", matchIfMissing = true)
public class DemoRestaurantSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRestaurantSeeder.class);

    /** The {@code owner} user in infrastructure/keycloak/keepdishesgoing-realm.json. */
    static final UUID DEMO_OWNER_ID = UUID.fromString("55801495-2bf9-4059-aa86-c524559409b9");

    /** Nominatim allows one lookup a second, and every restaurant costs one. */
    private static final long GEOCODING_PAUSE_MS = 1100;

    private final GetOwnerRestaurantQuery getOwnerRestaurantQuery;
    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final CreateDishUseCase createDishUseCase;
    private final PublishAndUnpublishDishUseCase publishDishUseCase;
    private final String imageBaseUrl;

    public DemoRestaurantSeeder(GetOwnerRestaurantQuery getOwnerRestaurantQuery,
                                CreateRestaurantUseCase createRestaurantUseCase,
                                CreateDishUseCase createDishUseCase,
                                PublishAndUnpublishDishUseCase publishDishUseCase,
                                @Value("${kdg.demo.image-base-url}") String imageBaseUrl) {
        this.getOwnerRestaurantQuery = getOwnerRestaurantQuery;
        this.createRestaurantUseCase = createRestaurantUseCase;
        this.createDishUseCase = createDishUseCase;
        this.publishDishUseCase = publishDishUseCase;
        this.imageBaseUrl = imageBaseUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        int created = 0;
        for (DemoRestaurant demo : DEMO_RESTAURANTS) {
            if (getOwnerRestaurantQuery.getOwnerRestaurant(demo.ownerId()).isPresent()) {
                continue;
            }
            try {
                if (created > 0) {
                    Thread.sleep(GEOCODING_PAUSE_MS);
                }
                seed(demo);
                created++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException e) {
                // Demo data is a convenience. A failure here must not stop the
                // application from serving everything else.
                log.warn("Could not seed demo restaurant {}: {}", demo.name(), e.getMessage());
            }
        }
        if (created > 0) {
            log.info("Seeded {} demo restaurant(s). Sign in as owner/owner to manage {}",
                    created, DEMO_RESTAURANTS.get(0).name());
        }
    }

    private void seed(DemoRestaurant demo) {
        Restaurant restaurant = createRestaurantUseCase.createRestaurant(new CreateRestaurantCommand(
                demo.ownerId(),
                demo.name(),
                demo.address(),
                demo.contactEmail(),
                demo.cuisine(),
                demo.preparationMinutes(),
                openAllDay(),
                List.of(picture(demo.slug()))
        ));
        UUID restaurantId = restaurant.getRestaurantId().uuid();

        for (DemoDish dish : demo.dishes()) {
            Dish created = createDishUseCase.createDish(new CreateDishCommand(
                    restaurantId, dish.name(), dish.type(), dish.tags(), dish.description(),
                    dish.price(), picture(slug(dish.name()))));
            publishDishUseCase.publishDish(new PublishDishCommand(restaurantId, created.getDishId().uuid()));
        }
        log.info("Demo restaurant {} created with {} dishes", demo.name(), demo.dishes().size());
    }

    /** Open around the clock, so the demo can be ordered from whenever it is run. */
    private static OpeningHours openAllDay() {
        Map<DayOfWeek, TimeRange> schedule = new EnumMap<>(DayOfWeek.class);
        Arrays.stream(DayOfWeek.values())
                .forEach(day -> schedule.put(day, new TimeRange(LocalTime.MIN, LocalTime.of(23, 59, 59))));
        return new OpeningHours(schedule);
    }

    private static String slug(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-");
    }

    /** A photo in src/main/resources/images, named after the restaurant's or dish's slug. */
    private String picture(String slug) {
        return imageBaseUrl + "/" + slug + ".jpg";
    }

    // ==================== The demo catalogue ====================

    private record DemoRestaurant(UUID ownerId, String slug, String name, CuisineType cuisine,
                                  Address address, String contactEmail, int preparationMinutes,
                                  List<DemoDish> dishes) {
    }

    private record DemoDish(String name, DishType type, Set<FoodTag> tags, String description, BigDecimal price) {
    }

    private static DemoDish dish(String name, DishType type, String price, String description, FoodTag... tags) {
        return new DemoDish(name, type, Set.of(tags), description, new BigDecimal(price));
    }

    private static UUID browseOnlyOwner(int n) {
        return UUID.fromString("00000000-0000-4000-8000-00000000000" + n);
    }

    private static final List<DemoRestaurant> DEMO_RESTAURANTS = List.of(
            new DemoRestaurant(DEMO_OWNER_ID, "petite-maison", "La Petite Maison", CuisineType.FRENCH,
                    new Address("Brusselstraat", 5, "2018", "Antwerpen", "Belgium"),
                    "contact@lapetitemaison.example", 30, List.of(
                    dish("Soupe à l'oignon", DishType.STARTER, "9.50", "Onion soup under a lid of melted gruyère."),
                    dish("Waterzooi", DishType.MAIN, "19.50", "Creamy chicken stew, the way Ghent makes it.", FoodTag.GLUTEN_FREE),
                    dish("Boeuf bourguignon", DishType.MAIN, "24.00", "Beef slow-cooked in red wine with mushrooms.", FoodTag.LACTOSE_FREE),
                    dish("Ratatouille", DishType.MAIN, "17.00", "Provençal vegetables, roasted slowly.", FoodTag.VEGAN, FoodTag.GLUTEN_FREE),
                    dish("Crème brûlée", DishType.DESSERT, "8.50", "Vanilla custard with a burnt sugar crust.", FoodTag.GLUTEN_FREE))),

            new DemoRestaurant(browseOnlyOwner(2), "trattoria-schelde", "Trattoria Schelde", CuisineType.ITALIAN,
                    new Address("Kloosterstraat", 40, "2000", "Antwerpen", "Belgium"),
                    "ciao@trattoriaschelde.example", 20, List.of(
                    dish("Bruschetta", DishType.STARTER, "7.00", "Grilled bread, tomato, garlic and basil.", FoodTag.VEGAN),
                    dish("Margherita", DishType.MAIN, "12.00", "Tomato, mozzarella and basil on a wood-fired base."),
                    dish("Penne arrabbiata", DishType.MAIN, "13.50", "Penne in a tomato sauce with chilli.", FoodTag.VEGAN, FoodTag.SPICY),
                    dish("Tiramisu", DishType.DESSERT, "6.50", "Coffee-soaked ladyfingers and mascarpone."))),

            new DemoRestaurant(browseOnlyOwner(3), "sakura-ramen", "Sakura Ramen", CuisineType.JAPANESE,
                    new Address("Ossenmarkt", 12, "2000", "Antwerpen", "Belgium"),
                    "hello@sakuraramen.example", 25, List.of(
                    dish("Gyoza", DishType.STARTER, "6.00", "Pan-fried pork dumplings, six pieces.", FoodTag.LACTOSE_FREE),
                    dish("Tonkotsu ramen", DishType.MAIN, "16.50", "Pork bone broth, chashu, egg and noodles.", FoodTag.LACTOSE_FREE),
                    dish("Spicy miso ramen", DishType.MAIN, "16.00", "Miso broth with chilli oil and tofu.", FoodTag.VEGAN, FoodTag.SPICY),
                    dish("Mochi", DishType.DESSERT, "5.50", "Three rice cakes: matcha, sesame and mango.", FoodTag.GLUTEN_FREE))),

            new DemoRestaurant(browseOnlyOwner(4), "frituur-de-leeuw", "Frituur De Leeuw", CuisineType.BELGIAN,
                    new Address("Groenplaats", 1, "2000", "Antwerpen", "Belgium"),
                    "frieten@deleeuw.example", 10, List.of(
                    dish("Small fries", DishType.MAIN, "3.50", "Twice-fried Belgian fries with mayonnaise.", FoodTag.LACTOSE_FREE),
                    dish("Stoofvlees with fries", DishType.MAIN, "11.00", "Beef stewed in brown beer, with a portion of fries."),
                    dish("Bicky burger", DishType.MAIN, "5.50", "The frituur classic, with its three sauces."),
                    dish("Waffle", DishType.DESSERT, "4.00", "Brussels waffle with powdered sugar.")))
    );
}
