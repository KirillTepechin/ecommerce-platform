package com.example.order.model.embedded;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class Customer {
    private String customerId;
    private String customerEmail;
    private String customerName;
}
