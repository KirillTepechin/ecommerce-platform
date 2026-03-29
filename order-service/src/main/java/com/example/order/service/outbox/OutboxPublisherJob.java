package com.example.order.service.outbox;

import com.example.order.model.outbox.OutboxEvent;
import com.example.order.model.outbox.OutboxStatus;
import com.example.order.repository.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherJob {

    private final OutboxEventRepository outboxEventRepository;
    private final OrderOutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:3000}")
    public void publish() {
        List<OutboxEvent> events = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);
        for (OutboxEvent outboxEvent : events) {
            try {
                OrderCreatedEvent event = objectMapper.readValue(outboxEvent.getPayload(), OrderCreatedEvent.class);
                kafkaTemplate.executeInTransaction(operations -> {
                    operations.send("order-created", event.getCustomerId(), event);
                    return true;
                });
                outboxService.markPublished(outboxEvent);
            } catch (Exception ex) {
                log.error("Failed to publish outbox event {}", outboxEvent.getId(), ex);
                outboxService.markFailed(outboxEvent);
            }
        }
    }
}
