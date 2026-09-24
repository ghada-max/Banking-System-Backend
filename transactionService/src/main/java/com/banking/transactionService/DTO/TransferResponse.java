package com.banking.transactionService.DTO;


import com.banking.transactionService.entity.TransactionStatus;
import com.banking.transactionService.entity.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class TransferResponse {

    private String id;

    private String senderAccountumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private TransactionType type;

    private TransactionStatus status;
    private String Description;
    private String referenceNumber;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;





}
