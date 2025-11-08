package com.example.order.service;

import com.example.order.mapper.OrderMapper;
import com.example.order.model.Order;
import event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OrderMapper orderMapper;

    public void publishOrderCreatedEvent(Order order) {
        OrderCreatedEvent event = orderMapper.toCreatedEvent(order);
        kafkaTemplate.send("order-created", event);
        log.info("Published OrderCreated event for order: {}", order.getId());
    }
}
