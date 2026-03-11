package com.example.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @Valid
    @NotNull
    private CreateOrderAddressRequest address;

    @NotEmpty
    @Valid
    private List<CreateOrderOrderItemRequest> items;
}
