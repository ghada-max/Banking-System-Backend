package com.banking.PaymentServiService.service;

import com.banking.PaymentServiService.dto.PaymentRequest;
import com.banking.PaymentServiService.dto.PaymentResponse;
import com.banking.PaymentServiService.entity.Payment;
import com.banking.PaymentServiService.entity.PaymentStatus;
import com.banking.PaymentServiService.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final  KafkaTemplate<String,Object> kafkaTemplate;
    private final PaymentRepository repo;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    private static final String PAYMENT_COMPLETED_TOPIC="payment.completed";
    private static final String PAYMENT_FAILED_TOPIC="payment.failed";

    public PaymentResponse createPaymentOrder(PaymentRequest request) throws RazorpayException {
        return null;
    }


    public void handleWebHook(Map<String, Object> payload) {

     log.info("Received Razorpay webhook:{]",payload.get("event"));

     String event=(String) payload.get("event");

     if("payment.captured".equals(event)){

         handlePaymentSuccess(payload);
     }
        else if("payment.failed".equals(event)){

            handlePaymentFailure(payload);
        }



    }

    private void handlePaymentFailure(Map<String, Object> payload) {

    try{
        Map<String,Object> paymentData=extractPaymentData(payload);
        String orderId=(String) paymentData.get("order_id");
        Payment payment=repo.findByRazorpayOrderId(orderId)
                .orElseThrow(()->new RuntimeException(
                        "payment not found for order"+orderId
                ));
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("payment failed via razorpay");
        repo.save(payment);
        Map<String,Object> event=new HashMap<>();
        event.put("paymentId",payment.getId());
        event.put("accountNumber",payment.getAccountNumber());
        event.put("amount",payment.getAmount());
        event.put("reason",payment.getFailureReason());

        kafkaTemplate.send( PAYMENT_FAILED_TOPIC,payment.getId(),event);
        log.info("payment Failed:{}",payment.getId());
    }catch(Exception e){
        log.info("error handling payment failure:{}", e.getMessage());


    }









    }

    private Map<String, Object> extractPaymentData(Map<String, Object> payload) {
        return null;
    }

    private void handlePaymentSuccess(Map<String, Object> payload) {
    try{

        Map<String,Object> paymentData=extractPaymentData(payload);
        String orderId=(String) paymentData.get("order_id");
        String paymentId=(String) paymentData.get("id");

        Payment payment=repo.findByRazorpayOrderId(orderId)
                .orElseThrow(()-> new RuntimeException("Payment " +
                        "not found for order: "+orderId));



        payment.setRazorpayPaymentId(paymentId);
        payment.setStatus(PaymentStatus.COMPLETED);
        repo.save(payment);
        //publish payment Completed


        Map<String,Object> event=new HashMap<>();
        event.put("paymentId",payment.getId());
        event.put("accountNumber",payment.getAccountNumber());
        event.put("amount",payment.getAmount());
        event.put("razorPaymentId",paymentId);
        kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC,payment.getId(),event);
        log.info("payment completed:{}",payment.getId());
    }catch(Exception e){

        log.info("error handling payment service:{}", e.getMessage());

    }


    }
}
