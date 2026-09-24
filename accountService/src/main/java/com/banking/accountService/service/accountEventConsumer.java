package com.banking.accountService.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class accountEventConsumer {
    private final accountService service;


    @KafkaListener(topics="transcation-completed")
    public void consumeTransactionCompleted(@Payload Map<String,Object> payload){
        try{
            String receiverAccount=(String) payload.get("receiveraccountNumber");
            BigDecimal amount=new BigDecimal(payload.get("amount").toString());
            log.info("crediting account:{} amount: {}",receiverAccount, amount);
            service.creditBalance(receiverAccount,amount);
        }catch (Exception e){
            log.error("error crediting amount",e.getMessage());
        }
    }

    @KafkaListener(topics="fraud-detected ")
    public void consumeFraudDetection(@Payload Map<String, Object> payload){
        try{
            String accountNumber=payload.get("accountNumber").toString();
            log.info("fraud detected: {}", accountNumber);
            service.blockAccount(accountNumber);
        }catch(Exception e){


        }
    }
}
