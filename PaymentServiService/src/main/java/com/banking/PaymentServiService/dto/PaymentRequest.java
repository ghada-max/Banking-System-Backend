package com.banking.PaymentServiService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    @NotBlank(message="Account number is required")
    private String accountNumber;

    @NotNull(message="Ammount is required")
    @Positive(message="ammount must be positive")
    private BigDecimal amount;

    private String description;
}
