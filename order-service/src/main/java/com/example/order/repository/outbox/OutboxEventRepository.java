package com.example.order.repository.outbox;

import com.example.order.model.outbox.OutboxEvent;
import com.example.order.model.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
