package com.example.order.service.outbox;

import com.example.order.model.Order;
import com.example.order.model.outbox.OutboxEvent;
import com.example.order.model.outbox.OutboxStatus;
import com.example.order.repository.outbox.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderOutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void addOrderCreatedEvent(Order order, OrderCreatedEvent event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(UUID.randomUUID());
        outboxEvent.setAggregateType("ORDER");
        outboxEvent.setAggregateId(String.valueOf(order.getId()));
        outboxEvent.setEventType(OrderCreatedEvent.class.getName());
        outboxEvent.setPayload(toJson(event));
        outboxEvent.setStatus(OutboxStatus.NEW);
        outboxEventRepository.save(outboxEvent);
    }

    public void markPublished(OutboxEvent outboxEvent) {
        outboxEvent.setStatus(OutboxStatus.PUBLISHED);
        outboxEvent.setPublishedAt(LocalDateTime.now());
        outboxEventRepository.save(outboxEvent);
    }

    public void markFailed(OutboxEvent outboxEvent) {
        outboxEvent.setStatus(OutboxStatus.FAILED);
        outboxEventRepository.save(outboxEvent);
    }

    private String toJson(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize outbox event", e);
        }
    }
}
