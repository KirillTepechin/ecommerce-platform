package com.example.payment.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemEvent {
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
}
