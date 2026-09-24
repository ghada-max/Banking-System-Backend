package com.banking.accountService.controller;

import com.banking.accountService.dto.AccountRequest;
import com.banking.accountService.dto.AccountResponse;
import com.banking.accountService.service.accountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;


@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Slf4j
public class accountController {
    private final accountService service;

     @PostMapping("/createAccount")
    public ResponseEntity<BigDecimal> createAccount(@Valid @RequestBody AccountRequest  request)
    {
  return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(request));
    }

    @GetMapping("/{AccountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String AccountNumber)
    {
        return ResponseEntity.ok(accountService.getAccount(AccountNumber));
    }

    @GetMapping("/{AccountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String AccountNumber)
    {
        return ResponseEntity.ok(accountService.getBalance(AccountNumber));
    }

    @PutMapping("/{AccountNumber}/block")
    public  void blockAccount(@PathVariable String AccountNumber)
    {
        accountService.blockAccount(AccountNumber);
    }

    @PutMapping("/{AccountNumber}/deduct")
    public ResponseEntity<String> deductBalance(@PathVariable String AccountNumber
    , @RequestParam BigDecimal amount)
    {
        service.deductBalance(AccountNumber,amount);
        return ResponseEntity.ok("balance deducted Successfully");
    }

    @PutMapping("/{AccountNumber}/credit")
    public ResponseEntity<String> creditBalance(
            @PathVariable String AccountNumber,
            @RequestParam BigDecimal amount
    ){
         service.creditBalance(AccountNumber,amount);
         return ResponseEntity.ok("balance credited successfully");
    }


}

















