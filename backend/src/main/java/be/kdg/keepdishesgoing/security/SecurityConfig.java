package be.kdg.keepdishesgoing.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // US-30: customers order as guests, so this must stay open.
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()

                        // US-24/25: tracking is open for the same reason. A guest
                        // order has no account to bind the check to, so the order's
                        // random id is what proves the caller placed it, and it is only
                        // ever handed to whoever completed the checkout.
                        .requestMatchers(HttpMethod.GET, "/api/orders/*").permitAll()

                        // Listed before the public catalogue below: "me" would
                        // otherwise be matched as a restaurant id and served to
                        // anyone.
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/me").hasRole("owner")

                        // US-14…18: customers browse the catalogue before deciding
                        // whether to order at all, so none of it can require a login.
                        .requestMatchers(HttpMethod.GET, "/api/restaurants").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/restaurants/*/dishes").permitAll()

                        // Owner routes. The role proves the caller is *an* owner;
                        // proving they own *this* restaurant needs a lookup, so the
                        // endpoints additionally carry @PreAuthorize on restaurantId.
                        .requestMatchers(HttpMethod.POST, "/api/restaurants").hasRole("owner")
                        // PATCH sets a manual override, DELETE removes it again.
                        .requestMatchers("/api/restaurants/*/status").hasRole("owner")
                        .requestMatchers("/api/restaurants/*/orders/**").hasRole("owner")
                        .requestMatchers("/api/*/dishes/**").hasRole("owner")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(mgmt -> mgmt.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173")); // React dev server
        // PATCH is used by the manual open/close toggle; without it the browser
        // preflight fails before the request is ever sent.
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return jwtConverter;
    }

    /**
     * Converts Keycloak's realm roles to Spring Security authorities
     * (adds prefix ROLE_ to match Spring's .hasRole() convention)
     */
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            Object realmAccessObj = jwt.getClaims().get("realm_access");
            if (!(realmAccessObj instanceof Map<?, ?> realmAccess)) {
                return List.of();
            }

            Object rolesObj = realmAccess.get("roles");
            if (!(rolesObj instanceof List<?> roles)) {
                return List.of();
            }

            return roles.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
    }

}
