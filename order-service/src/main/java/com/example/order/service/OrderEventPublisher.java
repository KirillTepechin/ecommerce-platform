package com.example.order.service;

import com.example.order.mapper.OrderMapper;
import com.example.order.model.Order;
import event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final OrderMapper orderMapper;

    public OrderCreatedEvent buildOrderCreatedEvent(Order order) {
        return orderMapper.toCreatedEvent(order);
    }
}
