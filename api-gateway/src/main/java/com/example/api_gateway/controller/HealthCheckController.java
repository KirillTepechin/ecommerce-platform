package com.example.api_gateway.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final WebClient.Builder webClientBuilder;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @GetMapping("/circuit-breakers")
    public ResponseEntity<Map<String, Object>> getCircuitBreakersStatus() {
        var breakers = circuitBreakerRegistry.getAllCircuitBreakers();

        Map<String, Object> status = breakers.stream()
                .collect(java.util.stream.Collectors.toMap(
                        CircuitBreaker::getName,
                        entry -> Map.of(
                                "state", entry.getState(),
                                "failureRate", entry.getMetrics().getFailureRate(),
                                "bufferedCalls", entry.getMetrics().getNumberOfBufferedCalls(),
                                "failedCalls", entry.getMetrics().getNumberOfFailedCalls(),
                                "notPermittedCalls", entry.getMetrics().getNumberOfNotPermittedCalls()
                        )
                ));

        return ResponseEntity.ok(Map.of(
                "timestamp", LocalDateTime.now(),
                "circuitBreakers", status
        ));
    }

    @GetMapping("/test/order-service")
    public Mono<ResponseEntity<Map<String, ? extends Serializable>>> testOrderService() {
        String orderServiceUrl = "http://localhost:8080/order-service/actuator/health";

        return webClientBuilder.build()
                .get()
                .uri(orderServiceUrl)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    Map<String, Serializable> body = new HashMap<>();
                    body.put("service", "order-service");
                    body.put("status", "UP");
                    body.put("timestamp", LocalDateTime.now());
                    body.put("response", response);
                    return ResponseEntity.<Map<String, ? extends Serializable>>ok(body);
                })
                .onErrorResume(e -> {
                    Map<String, Serializable> body = new HashMap<>();
                    body.put("service", "order-service");
                    body.put("status", "DOWN");
                    body.put("timestamp", LocalDateTime.now());
                    body.put("error", e.getMessage());
                    body.put("fallback", true);
                    return Mono.just(ResponseEntity
                            .status(503)
                            .body(body));
                });
    }

    @GetMapping("/test/circuit-breaker")
    public Mono<ResponseEntity<Map<String, ? extends Serializable>>> testCircuitBreaker() {
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8080/order-service/orders")
                .retrieve()
                .bodyToMono(String.class)
                .map(_ -> {
                    Map<String, Serializable> body = new HashMap<>();
                    body.put("message", "Request successful through circuit breaker");
                    body.put("timestamp", LocalDateTime.now());
                    return ResponseEntity.<Map<String, ? extends Serializable>>ok(body);
                })
                .onErrorResume(e -> {
                    log.error("Circuit breaker test failed: {}", e.getMessage());
                    Map<String, Serializable> body = new HashMap<>();
                    body.put("message", "Circuit breaker fallback activated");
                    body.put("error", e.getMessage());
                    body.put("timestamp", LocalDateTime.now());
                    body.put("fallback", true);
                    return Mono.just(ResponseEntity
                            .status(503)
                            .body(body));
                });
    }

}
