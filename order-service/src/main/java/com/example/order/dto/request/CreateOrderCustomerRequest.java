package com.example.order.dto.request;

import lombok.Data;

@Data
public class CreateOrderCustomerRequest {
    private String customerId;
    private String customerEmail;
    private String customerName;
}
