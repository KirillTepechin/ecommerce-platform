package com.example.order.controller;

import com.example.order.dto.OrderDto;
import com.example.order.model.enums.OrderStatus;
import com.example.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/by-status")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        final List<OrderDto> responses = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(responses);
    }
}
