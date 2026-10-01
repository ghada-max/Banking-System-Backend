package com.banking.fraudDetection.Client;


import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
@EnableFeignClients
@FeignClient(name="account-service",url="${account.service.url}")
public interface AccountServiceClient {

    @GetMapping("/api/account/{AccountNumber}/balance")
    BigDecimal getBalance(
            @PathVariable String AccountNumber
    );
}
