package com.banking.transactionService.DTO;

import com.banking.transactionService.entity.TransactionStatus;
import com.banking.transactionService.entity.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TransferRequest {

    private String id;

    @NotBlank(message="senderAccountumber is required ")
    private String senderAccountumber;
    @NotBlank(message="receiverAccountNumber is required ")
    private String receiverAccountNumber;
    @NotBlank(message="amount is required ")
    @Positive(message="amount must be positive")
    private BigDecimal amount;

    private String Description;





}
