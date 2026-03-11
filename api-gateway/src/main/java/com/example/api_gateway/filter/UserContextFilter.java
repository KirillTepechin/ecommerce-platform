package com.example.api_gateway.filter;

import com.example.api_gateway.security.JwtExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class UserContextFilter extends AbstractGatewayFilterFactory<UserContextFilter.Config> {

    private final JwtExtractor jwtExtractor;

    public UserContextFilter(JwtExtractor jwtExtractor) {
        super(Config.class);
        this.jwtExtractor = jwtExtractor;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> Mono.zip(
                        jwtExtractor.extractCustomerId(),
                        jwtExtractor.extractUsername(),
                        jwtExtractor.extractEmail(),
                        jwtExtractor.extractRoles()
                ).map(tuple -> {
                    String customerId = tuple.getT1();
                    String username = tuple.getT2();
                    String email = tuple.getT3();
                    List<String> roles = tuple.getT4();

                    // Добавляем заголовки
                    ServerHttpRequest mutatedRequest = exchange.getRequest()
                            .mutate()
                            .header("X-User-Id", customerId)
                            .header("X-Username", username)
                            .header("X-User-Email", email)
                            .header("X-User-Roles", String.join(",", roles))
                            .header("X-Authenticated", "true")
                            .build();

                    return exchange.mutate().request(mutatedRequest).build();
                })
                .defaultIfEmpty(exchange)
                .flatMap(chain::filter);
    }

    /**
     * Конфигурация фильтра
     */
    public static class Config {
        // Можно добавить параметры конфигурации:
        // - enabled: boolean
        // - headerPrefix: String
        // - includeClaims: List<String>
    }
}
