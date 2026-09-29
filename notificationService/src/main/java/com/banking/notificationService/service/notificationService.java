package com.banking.notificationService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class notificationService {
    @KafkaListener(topics="otp.generatedTopic")
    public void consumeGeneratedOTP(@Payload Map<String,Object> payload){
        try{
            String accountNumber=(String) payload.get("accountNumber");
            String otp=(String) payload.get("otp");
            String transactionId=(String) payload.get("transactionId");
            String amount= payload.get("amount").toString();
            String reason=(String) payload.get("reason");

            sendAlert("TRANSACTION VERIFICATION REQUIRED",String.format("suspicious activity detected on your account"));
        }catch(Exception e){
              log.error("error sending otp",e.getMessage());
        }
    }

    private void sendAlert(String subject,String message){

    }
}
