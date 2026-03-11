package com.example.order.controller;

import com.example.order.dto.request.CreateOrderRequest;
import com.example.order.dto.OrderDto;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Создание нового заказа
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        final OrderDto response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Получение заказа клиента
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {
        final OrderDto response = orderService.getCustomerOrder(orderId);
        return ResponseEntity.ok(response);
    }

    // Получение всех заказов клиента
    @GetMapping
    public ResponseEntity<List<OrderDto>> getCustomerOrders(Pageable pageable) {
        final List<OrderDto> responses = orderService.getCustomerOrders(pageable);
        return ResponseEntity.ok(responses);
    }

    // Отмена заказа клиентом
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(@PathVariable Long orderId) {

        final OrderDto response = orderService.cancelOrderByCustomer(orderId);
        return ResponseEntity.ok(response);
    }

    // Получение истории статусов заказа
    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<OrderDto> trackOrder(@PathVariable Long orderId) {

        final OrderDto response = orderService.getCustomerOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
