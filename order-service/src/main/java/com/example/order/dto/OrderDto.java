package com.example.order.dto.response;

import com.example.order.dto.AddressDto;
import com.example.order.dto.CustomerDto;
import com.example.order.dto.OrderItemDto;
import com.example.order.model.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderResponse {
    private Long id;
    private CustomerDto customer;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private AddressDto shippingAddress;
    private List<OrderItemDto> items;
}
