package com.example.api_gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        return WebClient.builder()
                .filter((request, next) -> next.exchange(request)
                        .transform(CircuitBreakerOperator.of(
                                (CircuitBreaker) circuitBreakerFactory.create(request.url() + "CircuitBreaker")
                        )))
                .build();
    }

}
