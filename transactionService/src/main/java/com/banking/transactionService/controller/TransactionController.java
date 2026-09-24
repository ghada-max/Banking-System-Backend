package com.banking.transactionService.controller;


import com.banking.transactionService.DTO.TransferRequest;
import com.banking.transactionService.DTO.TransferResponse;

import com.banking.transactionService.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService service;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request
            ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.transfer(request));
    }
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransferResponse> getTransactionById(
             @PathVariable String transactionId
    ){
        return ResponseEntity.ok(service.getTransactionById(transactionId));
    }

    @GetMapping("/account/{AccountNumber}")
    public ResponseEntity<List<TransferResponse>> getTransactionHistory(
             @PathVariable String AccountNumber
    ){
        return ResponseEntity.ok(service.getTransactionHistory(AccountNumber));
    }

    @PostMapping("/account/{AccountNumber}")
    public ResponseEntity<TransferResponse> verifyOTP(
             @PathVariable String transactionId,
             @RequestParam String otp
    ){
        log.info("verifyOTP transactionId:{} otp: {}", transactionId, otp)
    return ResponseEntity.ok(service.verifyOTP(transactionId,otp));
    }




}
