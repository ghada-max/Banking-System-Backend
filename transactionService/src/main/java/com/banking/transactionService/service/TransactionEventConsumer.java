package com.banking.transactionService.service;

import com.banking.transactionService.entity.Transaction;
import com.banking.transactionService.entity.TransactionStatus;
import com.banking.transactionService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionEventConsumer {
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final RedisTemplate<String,String> redisTemplate;
    private static final long OPT_EXPIRY_MINUTES=5;
    private static final String OTP_GENERATED_TOPIC = "otp.generatedTopic";
    private final TransactionRepository repo;
    @KafkaListener(topics="verification.required")
    public void consumeVerificationRequired(@Payload Map<String, Object> payload){

        try{
            String transactionId=(String) payload.get("transactionId");
            String accountNumber=(String) payload.get("accountNumber");
            String reason=(String) payload.get("reason");
            //change transaction status
            Transaction transfer=repo.findById(transactionId).orElseThrow(()->new RuntimeException(""));
            if( transfer.getStatus()!=TransactionStatus.PROCESSING){
                log.warn("transcation not processing-skipping");
                return;
            }
            //generateDigit
            String otp=String.format("%06d",(int) (Math.random()*900000)+100000);

            //store it in reddis
            String otpKey="verification:otp"+transactionId;
            redisTemplate.opsForValue().set(otpKey,otp,OPT_EXPIRY_MINUTES, TimeUnit.MINUTES);

            transfer.setStatus(TransactionStatus.PENDING_VERIFICATION);
            repo.save(transfer);
            log.info("otp generated for this transaction ");
            //Notify user
            Map<String,Object> otpEvent=new HashMap<>();
            otpEvent.put("transactionId",transactionId);
            otpEvent.put("amount",payload.get("amount"));
            otpEvent.put("accountNumber",accountNumber);
            otpEvent.put("reason",reason);
            otpEvent.put("otp",otp);
            kafkaTemplate.send(OTP_GENERATED_TOPIC ,transactionId,otpEvent);
        }catch(Exception e){
          log.error("error handling verification required",e.getMessage());

        }

    }
}
