package com.example.order.service.outbox;

import com.example.order.model.outbox.OutboxEvent;
import com.example.order.model.outbox.OutboxStatus;
import com.example.order.repository.outbox.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisherJob {

    private final OutboxEventRepository outboxEventRepository;
    private final OrderOutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${outbox.publisher.max-attempts:10}")
    private int maxAttempts;

    @Value("${outbox.publisher.retry-delay-seconds:5}")
    private int retryDelaySeconds;

    @Scheduled(fixedDelayString = "${outbox.publisher.fixed-delay-ms:3000}")
    public void publish() {
        List<OutboxEvent> events = outboxEventRepository.findBatchForPublish(
                OutboxStatus.NEW,
                OutboxStatus.RETRY,
                LocalDateTime.now()
        );

        for (OutboxEvent outboxEvent : events) {
            try {
                OrderCreatedEvent event = objectMapper.readValue(outboxEvent.getPayload(), OrderCreatedEvent.class);
                kafkaTemplate.send("order-created", String.valueOf(event.getOrderId()), event);
                outboxService.markPublished(outboxEvent);
            } catch (Exception ex) {
                log.error("Failed to publish outbox event {}", outboxEvent.getId(), ex);
                outboxService.scheduleRetry(outboxEvent, ex.getMessage(), maxAttempts, retryDelaySeconds);
            }
        }
    }
}
