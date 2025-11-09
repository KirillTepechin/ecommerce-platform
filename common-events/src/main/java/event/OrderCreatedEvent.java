package event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreatedEvent {
    private Long orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
    private LocalDateTime createdAt;
}
