package be.kdg.keepdishesgoing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.Modulith;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.scheduling.annotation.EnableScheduling;

@Modulith
@EnableScheduling
public class KeepDishesGoingApplication {

    private static final Logger log = LoggerFactory.getLogger(KeepDishesGoingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KeepDishesGoingApplication.class, args);
    }

    @EventListener(ApplicationStartedEvent.class)
    void onApplicationStarted() {
        ApplicationModules modules = ApplicationModules.of(KeepDishesGoingApplication.class);
        modules.forEach(module -> log.info("\n{}", module));
    }

}
