package com.example.order.dto.request;

import lombok.Data;

@Data
public class CreateOrderAddressRequest {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
