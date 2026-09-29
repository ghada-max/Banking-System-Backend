package com.banking.fraudDetection.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;
@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionEventConsumer {

    private final FraudDetectionService fraudDetectionService;
    @KafkaListener(topics="transaction_intiated",groupId="fraudDetectionGroup")
    public void consumerTransactionInitiated(@Payload Map<String,Object> payload){
        log.info("received transaction for fraud check: {}",payload.get("transactionId"));
      try{
       fraudDetectionService.checkIntiatedTransaction(payload);
      }catch(Exception e){


      }


    }
}
