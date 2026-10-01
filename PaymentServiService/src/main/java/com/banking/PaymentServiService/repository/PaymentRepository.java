package com.banking.PaymentServiService.repository;

import com.banking.PaymentServiService.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository  extends JpaRepository< Payment,String> {

    Optional<Payment> findByRazorpayOrderId(String orderId);
}
