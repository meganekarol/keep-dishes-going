package be.kdg.keepdishesgoing.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves the demo catalogue's photos from {@code src/main/resources/images} at
 * {@code /images/**}, so the seeded restaurants do not depend on an outside
 * image host. Owners still give their own pictures as URLs.
 */
@Configuration
public class DemoImagesConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**").addResourceLocations("classpath:/images/");
    }
}
