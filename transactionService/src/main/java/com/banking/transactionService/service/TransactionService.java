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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository  repo;
    private final AccountServiceClient client ;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TRANSACTION_INITIATED_TOPIC="transaction_intiated";
    private static final String TRANSACTION_COPMLETED_TOPIC="transaction_completed";
    private static final String TRANSACTION_REFUNDED_TOPIC="transaction_refunded";

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
    }
}
