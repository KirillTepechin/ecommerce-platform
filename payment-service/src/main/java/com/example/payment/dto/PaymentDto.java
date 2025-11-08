package com.example.payment.dto;

import com.example.payment.model.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Long orderId;
    private String customerId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
