package com.cakebox.controller;

import com.cakebox.service.PaymentService;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ----------------------------------------------------------
    // STEP 1: CREATE RAZORPAY ORDER
    //
    // React calls this when user clicks "Pay Now" on checkout page.
    // Returns razorpayOrderId + keyId needed to open payment popup.
    //
    // React code example:
    //   const res = await axios.post('/api/payment/create-order', { orderId: 5 });
    //   const options = {
    //     key: res.data.keyId,
    //     amount: res.data.amount,
    //     order_id: res.data.razorpayOrderId,
    //     ...
    //   };
    //   const rzp = new window.Razorpay(options);
    //   rzp.open();  // opens payment popup
    // ----------------------------------------------------------
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            Authentication auth,
            @RequestBody Map<String, Long> request) throws RazorpayException {

        Long orderId = request.get("orderId");
        return ResponseEntity.ok(paymentService.createRazorpayOrder(orderId));
    }

    // ----------------------------------------------------------
    // STEP 2: VERIFY PAYMENT
    //
    // After customer pays, Razorpay calls React's success handler.
    // React sends the 3 IDs to this endpoint for verification.
    //
    // React code example (inside Razorpay's handler.payment.captured):
    //   await axios.post('/api/payment/verify', {
    //     razorpayOrderId: response.razorpay_order_id,
    //     razorpayPaymentId: response.razorpay_payment_id,
    //     razorpaySignature: response.razorpay_signature
    //   });
    // ----------------------------------------------------------
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            Authentication auth,
            @RequestBody Map<String, String> request) {

        String razorpayOrderId = request.get("razorpayOrderId");
        String razorpayPaymentId = request.get("razorpayPaymentId");
        String razorpaySignature = request.get("razorpaySignature");

        return ResponseEntity.ok(
                paymentService.verifyPayment(
                        razorpayOrderId, razorpayPaymentId, razorpaySignature));
    }
}