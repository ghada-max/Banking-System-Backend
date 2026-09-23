package com.banking.accountService.dto;

import com.banking.accountService.entity.AccountStatus;
import com.banking.accountService.entity.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AccountRequest {

    @NotBlank(message="Account holder name is required ")
    private String accountHolderName;
    @NotBlank(message="email is required")
    private String email;
    @NotBlank(message="phone is required")
    @Email(message="Invalid Email Format")
    private String phone;
    @NotNull(message="Account type is required")
    private AccountType accountType;
    @NotNull(message="Account status is required")
    private AccountStatus accountStatus;


    @NotBlank(message="you must add an initial deposit")
    @Positive(message="initial deposit must be positive")
    private BigDecimal initialDeposit;
}
