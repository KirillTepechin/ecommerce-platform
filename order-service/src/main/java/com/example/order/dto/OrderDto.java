package com.example.order.dto;

import com.example.order.model.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDto {
    private Long id;
    private CustomerDto customer;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private AddressDto address;
    private List<OrderItemDto> items;
}
