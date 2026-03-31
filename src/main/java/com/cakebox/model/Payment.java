package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/Payment.java
//
// WHAT THIS FILE DOES:
// Stores Razorpay payment records.
// When a user pays, Razorpay gives us 3 IDs — we save all 3.
// These are used to VERIFY the payment was genuine.
//
// Razorpay Payment Flow:
//   1. We create an "order" in Razorpay → get razorpay_order_id
//   2. User pays → Razorpay gives razorpay_payment_id + signature
//   3. We verify the signature using our secret key
//   4. If valid → mark order as PAID
// =============================================================

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------------------------------------------------------
    // RAZORPAY IDs
    // All three are needed to verify a genuine payment.
    // ----------------------------------------------------------

    // Created by us when we call Razorpay API (step 1)
    @Column(name = "razorpay_order_id", unique = true)
    private String razorpayOrderId;

    // Given by Razorpay after user pays (step 2)
    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    // Cryptographic signature from Razorpay (step 2)
    // We verify this to confirm payment is authentic
    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    // ----------------------------------------------------------
    // AMOUNT
    // NOTE: Razorpay uses PAISE (1 rupee = 100 paise)
    // So ₹999 is sent as 99900 to Razorpay.
    // We store the actual rupee amount here for display.
    // ----------------------------------------------------------
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // "INR" for Indian Rupees
    @Column(nullable = false)
    private String currency = "INR";

    // ----------------------------------------------------------
    // PAYMENT METHOD (filled after payment)
    // Examples: "upi", "card", "netbanking", "wallet"
    // ----------------------------------------------------------
    @Column(name = "payment_method")
    private String paymentMethod;

    // ----------------------------------------------------------
    // STATUS
    // ----------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.CREATED;

    public enum PaymentStatus {
        CREATED,    // Razorpay order created, awaiting payment
        SUCCESS,    // Payment verified and confirmed
        FAILED,     // Payment attempt failed
        REFUNDED    // Refund issued
    }

    // ----------------------------------------------------------
    // TIMESTAMP
    // ----------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ----------------------------------------------------------
    // RELATIONSHIP: One Payment → One Order
    // ----------------------------------------------------------
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
}