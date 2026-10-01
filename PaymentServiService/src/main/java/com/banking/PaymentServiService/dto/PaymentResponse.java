package com.banking.PaymentServiService.dto;

import com.banking.PaymentServiService.entity.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class PaymentResponse {

    private String PaymentId;
    private String razorpayOrderId;

    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String razorpayKeyId;
}
