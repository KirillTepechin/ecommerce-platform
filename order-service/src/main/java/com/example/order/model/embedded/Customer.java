package com.example.order_service.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Customer {
    private String customerId;
    private String customerEmail;
    private String customerName;
}
