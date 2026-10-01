package com.banking.transactionService.service;

import com.banking.transactionService.DTO.TransferRequest;
import com.banking.transactionService.DTO.TransferResponse;
import com.banking.transactionService.TransactionInitiatedEvent;
import com.banking.transactionService.client.AccountServiceClient;
import com.banking.transactionService.entity.Transaction;
import com.banking.transactionService.entity.TransactionStatus;
import com.banking.transactionService.entity.TransactionType;
import com.banking.transactionService.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository  repo;
    private final AccountServiceClient client ;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String TRANSACTION_INITIATED_TOPIC="transaction_intiated";
    private static final String TRANSACTION_COPMLETED_TOPIC="transaction_completed";
    private static final String TRANSACTION_REFUNDED_TOPIC="transaction_refunded";
    private static final String FRAUD_DETECTED_EVENT="fraud-detected";

//deduct from sender
    //transactiontype processing
    //publish event to fraud detection
    //return

    public TransferResponse transfer(TransferRequest request) {

        log.info("SAGA START - transfer: {}->amount: {}",request.getSenderAccountumber(),request.getReceiverAccountNumber(),request.getAmount())
        client.deductBalance(request.getSenderAccountumber(),request.getAmount());
        Transaction transfer=new Transaction();
        transfer.setReceiverAccountNumber(request.getReceiverAccountNumber());
        transfer.setSenderAccountumber(request.getSenderAccountumber());
        transfer.setAmount(request.getAmount());
        transfer.setType(TransactionType.TRANSFER);
        transfer.setStatus(TransactionStatus.PROCESSING);
        transfer.setDescription(request.getDescription());
        transfer.setReferenceNumber(UUID.randomUUID().toString());
        Transaction savedTransaction=repo.save(transfer);

//publich event to kafka(transfer initiated)
        TransactionInitiatedEvent event=new TransactionInitiatedEvent(
                savedTransaction.getId(),
                savedTransaction.getSenderAccountumber(),
                savedTransaction.getReceiverAccountNumber(),
                savedTransaction.getAmount(),
                savedTransaction.getDescription()
        );

       kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC,savedTransaction.getId(),event);
       log.info("SAGA STEP N2,publich for fraud check");


        return mapToResponse(savedTransaction);
    }

    public TransferResponse mapToResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransferResponse response = new TransferResponse();
        response.setId(transaction.getId());
        response.setSenderAccountumber(transaction.getSenderAccountumber());
        response.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setReferenceNumber(transaction.getReferenceNumber());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());

        return response;
    }
    public TransferResponse getTransactionById(String transactionId) {
    Transaction transfer =repo.findById(transactionId).orElseThrow(()->new RuntimeException("transaction NotFount"));

    return mapToResponse(transfer);

    }

    public List<TransferResponse> getTransactionHistory(String accountNumber) {
      List<Transaction> transactions= repo.findBySenderAccountNumberOrderByDesc(accountNumber);
        return transactions.stream()
                .map(this::mapToResponse) // Appelle ta méthode mapToResponse pour chaque élément
                .collect(Collectors.toList());
    }

    public TransferResponse verifyOTP(String transactionId, String otp) {
     //verify OTP
       // of verify is failed,compensate sender notification
        //if verified
        //credit receiver and deduct balance from sender//send notificaton both sender and receiver

        log.info("OTP verification for the transaction",transactionId);
                Transaction transaction=repo.findById(transactionId).orElseThrow(()->
                        new RuntimeException("transaction not found"+transactionId));
                String otpKey="verification:otp"+transactionId;
                String storedOtp=redisTemplate.opsForValue().get("otpKey");

               if(storedOtp==null){

                   log.warn("OTP expired");
                   compensateTransaction(transaction,"otp expired and transaction expired and and amount refunded");
                   mapToResponse(transaction);
               }

               if(!storedOtp.equals(otp)){

                   log.warn("wrongOtp");
                   redisTemplate.delete(otpKey);
                   blockAccountAndCompensate(transaction,"wrong otp"+"account blocked for security");

                   return mapToResponse(transaction);
               }
               log.info("otp verified-transactionCompleted");

               redisTemplate.delete(otpKey);
               completeTransaction(transaction);
               return mapToResponse(transaction);

    }

    private void completeTransaction(Transaction transaction) {

        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        repo.save(transaction);
        client.creditBalance(transaction.getReceiverAccountNumber(),transaction.getAmount());
        Map<String,Object> completTransactionEvent=new HashMap<>();
        completTransactionEvent.put("transactionId",transaction.getId());
        completTransactionEvent.put("senderAccountNumber",transaction.getSenderAccountumber());
        completTransactionEvent.put("receiverAccountNumber",transaction.getReceiverAccountNumber());
        completTransactionEvent.put("amount",transaction.getAmount());
        kafkaTemplate.send(TRANSACTION_COPMLETED_TOPIC,transaction.getId(),completTransactionEvent);
        log.info("transaction completed successfully");


    }

    private void blockAccountAndCompensate(Transaction transaction,String reason){



        log.info("sender amount compensated and account blocked");
        //fraud event detected acooutn srvice will block account();

        Map<String,Object> fraudEvent=new HashMap<>();
        fraudEvent.put("transactionId",transaction.getId());
        fraudEvent.put("senderAccountNumber",transaction.getSenderAccountumber());
        fraudEvent.put("amount",transaction.getAmount());
        fraudEvent.put("reason",reason);
        kafkaTemplate.send(FRAUD_DETECTED_EVENT,transaction.getId(),fraudEvent);
        log.info("saga compensation completed");
        //refund sender - SAGA
        compensateTransaction(transaction,reason);
    }

    private void compensateTransaction(Transaction transaction, String reason) {

      //Feign Client

        client.creditBalance(transaction.getSenderAccountumber(),transaction.getAmount());
        transaction.setStatus(TransactionStatus.FLAGGED);
        transaction.setSetFailureReason(reason);
        repo.save(transaction);
        log.info("sender amount compensated");
        Map<String,Object> refundEvent=new HashMap<>();
        refundEvent.put("transactionId",transaction.getId());
        refundEvent.put("senderAccountNumber",transaction.getSenderAccountumber());
        refundEvent.put("amount",transaction.getAmount());
        refundEvent.put("reason",reason);
        kafkaTemplate.send(TRANSACTION_REFUNDED_TOPIC,transaction.getId(),refundEvent);
        log.info("saga compensation completed");
        //amount refunded;
    }


    public void processCleanResult(String transactionId) {
   Transaction transfer=repo.findById(transactionId).orElseThrow(()->new
           RuntimeException("transaction Id not Found"));
        completeTransaction(transfer);

        if(transfer.getStatus()!= TransactionStatus.PROCESSING){

            log.warn("transaction not processing");
            return;
        }
        completeTransaction(transfer);
    }
}



