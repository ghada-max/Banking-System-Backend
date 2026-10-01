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

            sendAlert("TRANSACTION VERIFICATION REQUIRED",String.format("suspicious activity detected on your account"), String.format("%s debited from account %s", amount, accountNumber));
        }catch(Exception e){
              log.error("error sending otp",e.getMessage());
        }
    }

    @KafkaListener(topics="transaction_completed")
    public void consumeTransactionCompleted(@Payload Map<String,Object> payload){
        try{
            String senderAccount=(String) payload.get("senderAccountNumber");
            String receiveAccount=(String) payload.get("reveiverAccountNumber");
            String amount=payload.get("amount").toString();
//debit alert
            sendAlert(senderAccount,
                    "Debit Alert",
                    String.format("%s debited from account %s",
                            amount,senderAccount
                            ));
//credit alert
            sendAlert(receiveAccount,
                    "Credit Alert",
                    String.format("%s Credited from account %s",
                            amount,receiveAccount
                    ));
        }catch(Exception e){
           log.info("error during sending completed transaction alert:{},senderAccount");
        }
    }


    @KafkaListener(topics="transaction_refunded")
    public void consumeTransactionRefunded(@Payload Map<String,Object> payload){
        try{
            String senderAccount=(String) payload.get("senderAccountNumber");
            String receiveAccount=(String) payload.get("reveiverAccountNumber");
            String amount=payload.get("amount").toString();

            sendAlert(senderAccount,
                    "Refund Alert",
                    String.format("%s Refunded from account %s",
                            amount,senderAccount
                    ));
        }catch(Exception e){
            log.info("error during sending completed transaction alert:{},senderAccount");
        }
    }

    @KafkaListener(topics="fraud-detected")
    public void consumeFraudDetectedEvent(@Payload Map<String,Object> payload){
        try{
            String senderAccount=(String) payload.get("senderAccountNumber");
            String reason=(String) payload.get("reason");
            sendAlert(senderAccount,
                    "Fraud detected Alert",
                    String.format("%Your account %s has been blocked"+"reason:%s "+"please contact your bank ummediately",
                            senderAccount,  reason
                    ));
        }catch(Exception e){
            log.info("error during sending fraud detection alert",e.getMessage());
        }
    }


    @KafkaListener(topics="payment.completed")
    public void consumePaymentCompleted(@Payload Map<String,Object> payload){
        try{
            String accountNumber=(String) payload.get("accountNumber");
            String amount=(String) payload.get("amount");
            sendAlert(accountNumber,
                    "Payment completed Alert",
                    String.format("payment of %s completed"+"razorpay Id:%s",amount,payload.get("razorpayPaymentId")
                    ));
        }catch(Exception e){
            log.info("error sending payment completed alert",e.getMessage());
        }
    }


    @KafkaListener(topics="payment.failed")
    public void consumePaymentFailed(@Payload Map<String,Object> payload){
        try{
            String accountNumber=(String) payload.get("accountNumber");
            String reason=(String) payload.get("reason");
            sendAlert(accountNumber,
                    "Payment Failed Alert",
                    String.format("payment of %s Failed"+"reason:%s",accountNumber,reason)
                    );
        }catch(Exception e){
            log.info("error sending payment completed alert",e.getMessage());
        }
    }



    private void sendAlert(String accountNumber, String subject, String message){
//one method for all Notification alerts types)

log.info("-----------");
log.info("account: {}",accountNumber);
log.info("subject: {}",subject);
 log.info("message: {}",message);
 log.info("-----------");






    }
}
