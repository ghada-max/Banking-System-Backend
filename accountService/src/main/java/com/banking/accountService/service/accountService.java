package com.banking.accountService.service;

import com.banking.accountService.dto.AccountRequest;
import com.banking.accountService.dto.AccountResponse;
import com.banking.accountService.entity.Account;
import com.banking.accountService.entity.AccountStatus;
import com.banking.accountService.entity.AccountType;
import com.banking.accountService.repository.accountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class accountService {
    private final accountRepository repo;
    private static SecureRandom secureRandom=new SecureRandom();
    public AccountResponse createAccount(AccountRequest request) {

        log.info("creating account for: {}",request.getEmail());

        if(repo.existsByEmail(request.getEmail())){

            throw new RuntimeException(("Account already exists for email: "+request.getEmail()));
        }

        Account account=new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setAccountType(request.getAccountType());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(
                request.getAccountType()== AccountType.SAVING? new BigDecimal(("100000")
                :new BigDecimal("500000")
                );
                Account savedAccount=repo.save(account);
                log.info("Account created{}", savedAccount.getAccountNumber());
                return mapToResponse(savedAccount);


    }

    private String generateAccountNumber() {

        String accountNumber;
        do{
            Long number=secureRandom.nextLong(1_000_000_000_000L);
            accountNumber=String.format("%012d",number);
        }while (repo.existsByAccountNumber(accountNumber));
       return accountNumber;
    }

    private AccountResponse mapToResponse(Account savedAccount) {
        AccountResponse response = new AccountResponse();
        response.setId(savedAccount.getId());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setAccountHolderName(savedAccount.getAccountHolderName());
        response.setEmail(savedAccount.getEmail());
        response.setPhone(savedAccount.getPhone());
        response.setAccountType(savedAccount.getAccountType());
        response.setAccountStatus(savedAccount.getAccountStatus());
        response.setBalance(savedAccount.getBalance());
        response.setDailyTransactionLimit(savedAccount.getDailyTransactionLimit());
        response.setCreatedAt(savedAccount.getCreatedAt());

        return response;
    }

    public  AccountResponse getAccount(String accountNumber) {
       Account acc= repo.findByAccountNumber(accountNumber).orElseThrow(
               ()->new RuntimeException("account number not found")
       );
        return(mapToResponse(acc));
    }

    public  BigDecimal getBalance(String accountNumber) {
        Account acc= repo.findByAccountNumber(accountNumber).orElseThrow(
                ()->new RuntimeException("account number not found")
        );
        return acc.getBalance();
    }

    public void blockAccount(String accountNumber) {
        Account acc= repo.findByAccountNumber(accountNumber).orElseThrow(
                ()->new RuntimeException("account number not found")
        );
        acc.setAccountStatus(AccountStatus.BLOCKED);
        repo.save(acc);
        log.info("account blocked");



    }

    public void  deductBalance(String accountNumber, BigDecimal amount) {
       log.info("deducting balance");
        Account acc= repo.findByAccountNumber(accountNumber).orElseThrow(
                ()->new RuntimeException("account number not found")
        );

        if(acc.getAccountStatus()!=AccountStatus.ACTIVE){
            throw new RuntimeException("account is not active");

        }
        if(acc.getBalance().compareTo(amount)<0){
            throw new RuntimeException(("insufficent funds for account"));
        }

        acc.setBalance(acc.getBalance().subtract(amount));
        repo.save(acc);

        log.info("balance updated: ",acc.getBalance());
    }



    public String creditBalance(String accountNumber, BigDecimal amount) {

        log.info("crediting balance ... ");
        Account acc= repo.findByAccountNumber(accountNumber).orElseThrow(
                ()->new RuntimeException("account number not found")
        );

        if(acc.getAccountStatus()!=AccountStatus.ACTIVE){
            throw new RuntimeException("account is not active");

        }

        acc.setBalance(acc.getBalance().add(amount));
        repo.save(acc);

        log.info(" balance credited: ",acc.getBalance());

    }
}






