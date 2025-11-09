package event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRejectedEvent {
    private Long orderId;
    private String customerId;
    private String reason;
    private LocalDateTime rejectedAt;
}
