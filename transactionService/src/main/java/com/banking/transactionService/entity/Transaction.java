package com.banking.transactionService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Table(name="transaction")
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private String id;

    private String senderAccountumber;

    private String receiverAccountNumber;
    @Column(nullable=false,precision=15,scale=2)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private TransactionStatus status;
    private String Description;
    private String referenceNumber;
    @CreationTimestamp
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;


}
