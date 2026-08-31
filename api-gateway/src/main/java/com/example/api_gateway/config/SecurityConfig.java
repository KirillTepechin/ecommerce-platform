package com.example.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // Публичные endpoints
                        .pathMatchers(
                                "/actuator/health/**",
                                "/actuator/info",
                                "/oauth2/**",
                                "/login/**",
                                "/fallback/**"
                        ).permitAll()

                        .pathMatchers("/actuator/**").hasRole("MONITOR")
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")

                        // Order endpoints
                        .pathMatchers("/api/orders/**").hasRole("CUSTOMER")

                        // По умолчанию требуем аутентификацию
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .oauth2Client(Customizer.withDefaults())
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080",
                "https://ecommerce.local",
                "http://localhost"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            Collection<String> realmRoles = (Collection<String>) realmAccess.get("roles");

            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            Collection<String> clientRoles = null;
            if (resourceAccess != null) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("ecommerce-frontend");
                clientRoles = clientAccess != null ?
                        (Collection<String>) clientAccess.get("roles") : Collections.emptyList();
            }

            // Объединяем роли
            Collection<String> allRoles = new ArrayList<>();
            if (realmRoles != null) allRoles.addAll(realmRoles);
            if (clientRoles != null) allRoles.addAll(clientRoles);

            return allRoles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

}
