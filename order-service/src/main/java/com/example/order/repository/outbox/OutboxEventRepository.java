package com.example.order.repository.outbox;

import com.example.order.model.outbox.OutboxEvent;
import com.example.order.model.outbox.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    @Query("""
            select e
            from OutboxEvent e
            where (e.status = :newStatus or e.status = :retryStatus)
              and e.nextRetryAt <= :now
            order by e.createdAt asc
            """)
    List<OutboxEvent> findBatchForPublish(
            @Param("newStatus") OutboxStatus newStatus,
            @Param("retryStatus") OutboxStatus retryStatus,
            @Param("now") LocalDateTime now
    );
}
