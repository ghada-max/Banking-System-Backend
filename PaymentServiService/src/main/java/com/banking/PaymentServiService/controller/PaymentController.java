package com.banking.PaymentServiService.controller;

import com.banking.PaymentServiService.dto.PaymentRequest;
import com.banking.PaymentServiService.dto.PaymentResponse;
import com.banking.PaymentServiService.service.PaymentService;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/api/Payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService service;

    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponse> createPaymentOrder (
            @Valid@RequestBody PaymentRequest request
            ) throws RazorpayException {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createPaymentOrder(request));
    }

    //Razorpay webhook endpoint
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody Map<String,Object> payload
    ){
      service.handleWebHook(payload);
      return ResponseEntity.ok("webhook processed");
    }


}
