package com.example.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Long id;

    private String productId;

    private String productName;

    private Integer quantity;

    private BigDecimal unitPrice;
}
