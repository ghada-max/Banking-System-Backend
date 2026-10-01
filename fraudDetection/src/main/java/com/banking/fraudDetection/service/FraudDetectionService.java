package com.banking.fraudDetection.service;

import com.banking.accountService.FraudCheckResult;
import com.banking.fraudDetection.Client.AccountServiceClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    @Value("{fraud.max-transactions-per-minute}")
    private int maxtransactionPerMinutes;

    @Value("{fraud.suspicious-amount-multiplier}")
    private double suspiciousAmountMultiplier;

    @Value("{fraud.max-balance-percentage}")
    private double maxBalancePercentage;

    private final AccountServiceClient client;
    private static final String VERIFICATION_REQUIRED_TOPIC="verification.required";
    private static final String FRAUD_CLEAN_RESULT ="fraud.clean";
    private final RedisTemplate<String,String> redisTemplate;
    private final KafkaTemplate<String,Object> kafkatemplate;
    public void checkIntiatedTransaction(Map<String, Object> payload) {
     String transactionId=(String)payload.get("transactionId");
     String accountNumber=(String)payload.get("SenderAccountNumber");
     BigDecimal amount=new BigDecimal(payload.get("amount").toString());

     BigDecimal senderBalance=client.getBalance(accountNumber);
     log.info("checking transaction account: {} amount: {} balance:{}", transactionId,amount,senderBalance);
     FraudCheckResult result=performFraudCheck(accountNumber,amount,senderBalance);

    if(result.isFraud()) {
        log.info("suspicious activity detected,account: {}"+"reason:{}",accountNumber,result.getReason());
        Map<String,Object> verificationEvent=new HashMap<>();
        verificationEvent.put("transactionId",transactionId);
        verificationEvent.put("amount",amount);
        verificationEvent.put("accountNumber",accountNumber);
        verificationEvent.put("reason",result.getReason());
        kafkatemplate.send(VERIFICATION_REQUIRED_TOPIC,transactionId,verificationEvent);
    }
    else{
        log.info("clean transaction  ,account: {}",accountNumber);
        Map<String,Object> transactioncleanEvent=new HashMap<>();
        transactioncleanEvent.put("transactionId",transactionId);
        transactioncleanEvent.put("amount",amount);
        transactioncleanEvent.put("accountNumber",accountNumber);
        kafkatemplate.send(FRAUD_CLEAN_RESULT,transactionId,transactioncleanEvent);

    }
    }

    private FraudCheckResult performFraudCheck(String accountNumber, BigDecimal amount, BigDecimal senderBalance) {
     if(isVelocityExceeded(accountNumber)){
         return new FraudCheckResult(true,"too many transactions in 60seconds"+ "velocity limit exceeded");
     }


     if(isAmmountSupicious(accountNumber,amount)){
         return new FraudCheckResult(true,"unusual transaction Amount it exceed 3 times your average ");
     }

     if(senderBalance.compareTo(BigDecimal.ZERO)>0 && isBalanceCheckFailed(senderBalance,amount))
     {

         return new FraudCheckResult(true,"transaction exeed 90% of account balance");
     }
    return new FraudCheckResult(false,null);
    }

    private boolean isVelocityExceeded(String accountNumber) {


     String key="fraud:velocity"+accountNumber;
     Long count=redisTemplate.opsForValue().increment(key);
     if(count!=null && count==1){
         redisTemplate.expire(key,60, TimeUnit.SECONDS);
     }
     log.info("velocity Check");
     return count!=0 && count>maxtransactionPerMinutes;



    }
    private boolean isAmmountSupicious(String accountNumber,BigDecimal amount) {

    String avgKey="fraud:avg_amount"+accountNumber;
    String avgStr=redisTemplate.opsForValue().get(avgKey);


    if(avgStr==null){
        redisTemplate.opsForValue().set(avgKey,amount.toString());
        return false;
    }
    BigDecimal avgAmount=new BigDecimal(avgStr);
    BigDecimal threshold=avgAmount.multiply(
            BigDecimal.valueOf(suspiciousAmountMultiplier));
    //updateRunningAverage
    BigDecimal newAvg=avgAmount.add(amount)
            .divide(BigDecimal.valueOf(2),2, RoundingMode.HALF_UP);


    redisTemplate.opsForValue().set(avgKey,newAvg.toString());

    return amount.compareTo(threshold)>0;

    }
    private boolean isBalanceCheckFailed(BigDecimal senderBalance, BigDecimal amount) {
    BigDecimal maxAllowed=senderBalance.multiply(
            BigDecimal.valueOf(maxBalancePercentage));
            return amount.compareTo(maxAllowed)>0;

    }

}
