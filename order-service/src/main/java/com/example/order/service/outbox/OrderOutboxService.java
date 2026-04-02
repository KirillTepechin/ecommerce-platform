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

    private static final int MAX_ERROR_LENGTH = 2000;

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
        outboxEvent.setAttempts(0);
        outboxEvent.setNextRetryAt(LocalDateTime.now());
        outboxEventRepository.save(outboxEvent);
    }

    public void markPublished(OutboxEvent outboxEvent) {
        outboxEvent.setStatus(OutboxStatus.PUBLISHED);
        outboxEvent.setPublishedAt(LocalDateTime.now());
        outboxEvent.setLastError(null);
        outboxEventRepository.save(outboxEvent);
    }

    public void scheduleRetry(OutboxEvent outboxEvent, String error, int maxAttempts, int retryDelaySeconds) {
        int newAttempts = outboxEvent.getAttempts() + 1;
        outboxEvent.setAttempts(newAttempts);
        outboxEvent.setLastError(limit(error));

        if (newAttempts >= maxAttempts) {
            outboxEvent.setStatus(OutboxStatus.DEAD);
        } else {
            outboxEvent.setStatus(OutboxStatus.RETRY);
            outboxEvent.setNextRetryAt(LocalDateTime.now().plusSeconds(retryDelaySeconds));
        }

        outboxEventRepository.save(outboxEvent);
    }

    private String toJson(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize outbox event", e);
        }
    }

    private String limit(String error) {
        if (error == null) {
            return null;
        }
        return error.length() > MAX_ERROR_LENGTH ? error.substring(0, MAX_ERROR_LENGTH) : error;
    }
}
