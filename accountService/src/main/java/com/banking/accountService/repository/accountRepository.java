package com.banking.accountService.repository;

import com.banking.accountService.dto.AccountResponse;
import com.banking.accountService.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface accountRepository extends JpaRepository<Account,String> {
    boolean existsByEmail(String email);
    boolean existsByAccountNumber(String accountNumber);

    Optional<Account>  findByAccountNumber(String accountNumber);
}
