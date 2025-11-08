package com.example.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateOrderOrderItemRequest {
    @NotBlank
    private String productId;
    @NotBlank
    private String productName;
    @NotNull
    private Integer quantity;
    @NotNull
    private BigDecimal unitPrice;
}
