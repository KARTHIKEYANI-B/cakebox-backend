package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/Order.java
//
// WHAT THIS FILE DOES:
// Stores placed orders.
// From your UI design, orders have:
//   - Status tracking: ORDER_PLACED → BAKING → OUT_FOR_DELIVERY → DELIVERED
//   - Delivery date and time slot
//   - Razorpay payment reference
//   - "Surprise Mode" toggle (don't call recipient)
// =============================================================

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------------------------------------------------------
    // ORDER STATUS
    // This is what powers the "Live Order Tracking" in your UI.
    //
    // Flow: ORDER_PLACED → CONFIRMED → BAKING → OUT_FOR_DELIVERY → DELIVERED
    //       (or CANCELLED at any point)
    //
    // @Enumerated → stores enum as text string in DB (not number)
    // ----------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.ORDER_PLACED;

    public enum OrderStatus {
        ORDER_PLACED,       // Customer just placed order
        CONFIRMED,          // Shop confirmed the order
        BAKING,             // Cake is being baked 🍰
        OUT_FOR_DELIVERY,   // Delivery agent picked up 🚚
        DELIVERED,          // Customer received it ✅
        CANCELLED           // Order was cancelled ❌
    }

    // ----------------------------------------------------------
    // PAYMENT STATUS
    // ----------------------------------------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    public enum PaymentStatus {
        PENDING,    // Waiting for payment
        PAID,       // Payment successful via Razorpay
        FAILED,     // Payment failed
        REFUNDED    // Money returned to customer
    }

    // ----------------------------------------------------------
    // PRICING
    // ----------------------------------------------------------
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "delivery_charge", precision = 10, scale = 2)
    private BigDecimal deliveryCharge = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // ----------------------------------------------------------
    // DELIVERY DETAILS
    // deliveryDate     → which day to deliver (from date picker in UI)
    // deliverySlot     → time slot e.g. "10:00 AM - 12:00 PM"
    // isExpressDelivery → true = 2-hour delivery (your UI feature)
    // ----------------------------------------------------------
    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "delivery_slot")
    private String deliverySlot;

    @Column(name = "is_express_delivery")
    private Boolean isExpressDelivery = false;

    // ----------------------------------------------------------
    // COUPON
    // ----------------------------------------------------------
    @Column(name = "coupon_code")
    private String couponCode;

    // ----------------------------------------------------------
    // SURPRISE MODE (your unique feature from UI design!)
    // When true: hide price, show secret message, don't call recipient
    // ----------------------------------------------------------
    @Column(name = "is_surprise_mode")
    private Boolean isSurpriseMode = false;

    @Column(name = "surprise_message", columnDefinition = "TEXT")
    private String surpriseMessage;

    @Column(name = "do_not_call_recipient")
    private Boolean doNotCallRecipient = false;

    // ----------------------------------------------------------
    // RAZORPAY PAYMENT IDs
    // Saved when payment is made — used to verify & track payment
    // ----------------------------------------------------------
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    // ----------------------------------------------------------
    // TIMESTAMP
    // ----------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ----------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Snapshot of address at time of order
    // (user might change address later, but order address stays)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", nullable = false)
    private Address deliveryAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
}