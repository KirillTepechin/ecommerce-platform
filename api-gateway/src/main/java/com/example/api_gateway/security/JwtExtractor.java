package com.example.api_gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtExtractor {

    /**
     * Извлекает customer_id из JWT токена
     * Ищет в claims: customer_id → preferred_username → sub
     */
    public Mono<String> extractCustomerId() {
        return getJwt()
                .map(jwt -> {
                    String preferredUsername = jwt.getClaimAsString("preferred_username");
                    if (preferredUsername != null && !preferredUsername.isEmpty()) {
                        return preferredUsername;
                    }

                    return jwt.getSubject();
                })
                .defaultIfEmpty("anonymous");
    }

    /**
     * Извлекает username из JWT токена
     */
    public Mono<String> extractUsername() {
        return getJwt()
                .map(jwt -> {
                    String preferredUsername = jwt.getClaimAsString("preferred_username");
                    return preferredUsername != null ? preferredUsername : jwt.getSubject();
                })
                .defaultIfEmpty("anonymous");
    }

    /**
     * Извлекает email из JWT токена
     */
    public Mono<String> extractEmail() {
        return getJwt()
                .map(jwt -> jwt.getClaimAsString("email"))
                .defaultIfEmpty("");
    }

    /**
     * Извлекает роли из JWT токена
     * Keycloak формат: realm_access.roles
     */
    public Mono<List<String>> extractRoles() {
        return getJwt()
                .map(jwt -> {
                    try {
                        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                        if (realmAccess != null) {
                            Object rolesObj = realmAccess.get("roles");
                            if (rolesObj instanceof List) {
                                return (List<String>) rolesObj;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Failed to extract roles from JWT", e);
                    }
                    return List.<String>of();
                })
                .defaultIfEmpty(List.of());
    }

    /**
     * Проверяет, имеет ли пользователь указанную роль
     */
    public Mono<Boolean> hasRole(String role) {
        return extractRoles()
                .map(roles -> roles.contains(role));
    }

    /**
     * Проверяет, является ли пользователь администратором
     */
    public Mono<Boolean> isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Вспомогательный метод для получения JWT
     */
    private Mono<Jwt> getJwt() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(authentication -> authentication != null && authentication.getPrincipal() instanceof Jwt)
                .map(authentication -> (Jwt) authentication.getPrincipal());
    }
}
