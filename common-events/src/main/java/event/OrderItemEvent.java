package event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItemEvent {
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
