package com.example.payment.mapper;

import com.example.payment.dto.PaymentDto;
import com.example.payment.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    PaymentDto toDto(Payment payment);

    List<PaymentDto> toDtoList(List<Payment> payments);
}
