package com.cakebox.service;

import com.cakebox.model.Order;
import com.cakebox.model.Payment;
import com.cakebox.repository.OrderRepository;
import com.cakebox.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    // ----------------------------------------------------------
    // STEP 1: CREATE RAZORPAY ORDER
    //
    // Flow:
    //   React clicks "Pay Now" → calls this endpoint
    //   We create an order in Razorpay → get razorpay_order_id
    //   We send razorpay_order_id + key_id back to React
    //   React opens Razorpay checkout popup with these details
    //   Customer pays via UPI/Card/Netbanking
    //
    // NOTE: Razorpay uses PAISE (1 rupee = 100 paise)
    //   ₹999 → send 99900 to Razorpay
    // ----------------------------------------------------------
    public Map<String, Object> createRazorpayOrder(Long orderId) throws RazorpayException {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        if (order.getPaymentStatus() == Order.PaymentStatus.PAID) {
            throw new RuntimeException("Order is already paid");
        }

        // Convert rupees to paise (multiply by 100)
        long amountInPaise = order.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        // Create Razorpay client with your credentials
        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        // Build Razorpay order request
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);          // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "cakebox_order_" + orderId);
        orderRequest.put("notes", new JSONObject()
                .put("orderId", orderId.toString())
                .put("customerName", order.getUser().getName())
        );

        // Create order in Razorpay
        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        log.info("Razorpay order created: {} for CakeBox order #{}", razorpayOrderId, orderId);

        // Save Payment record in our DB
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency("INR");
        payment.setStatus(Payment.PaymentStatus.CREATED);
        paymentRepository.save(payment);

        // Update order with razorpay order id
        order.setRazorpayOrderId(razorpayOrderId);
        orderRepository.save(order);

        // Return to React — these are used to open Razorpay checkout popup
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("razorpayOrderId", razorpayOrderId);
        response.put("amount", amountInPaise);          // in paise
        response.put("amountInRupees", order.getTotalAmount()); // for display
        response.put("currency", "INR");
        response.put("keyId", razorpayKeyId);           // React needs this to open popup
        response.put("orderId", orderId);
        response.put("customerName", order.getUser().getName());
        response.put("customerEmail", order.getUser().getEmail());
        response.put("customerPhone", order.getUser().getPhoneNumber() != null ?
                order.getUser().getPhoneNumber() : "");

        return response;
    }

    // ----------------------------------------------------------
    // STEP 2: VERIFY RAZORPAY PAYMENT
    //
    // After customer pays, Razorpay sends 3 values to React:
    //   razorpay_order_id   → the order ID we created
    //   razorpay_payment_id → unique ID of this payment
    //   razorpay_signature  → cryptographic proof of payment
    //
    // We MUST verify the signature using our secret key.
    // If signature matches → payment is genuine → mark order PAID
    // If signature doesn't match → someone tampered → reject
    //
    // Verification formula (from Razorpay docs):
    //   HMAC-SHA256(razorpay_order_id + "|" + razorpay_payment_id, secret)
    //   If result == razorpay_signature → VALID ✅
    // ----------------------------------------------------------
    public Map<String, Object> verifyPayment(String razorpayOrderId,
            String razorpayPaymentId, String razorpaySignature) {

        // Step 1: Find our Payment record
        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        // Step 2: Verify signature
        boolean isValid = verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (!isValid) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.warn("Payment verification FAILED for order: {}", razorpayOrderId);
            throw new RuntimeException("Payment verification failed. Please contact support.");
        }

        // Step 3: Signature valid! Update payment record
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setRazorpaySignature(razorpaySignature);
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // Step 4: Mark order as PAID and CONFIRMED
        orderService.markOrderAsPaid(payment.getOrder().getId(), razorpayOrderId);

        log.info("Payment VERIFIED for order #{} — ₹{}",
                payment.getOrder().getId(), payment.getAmount());

        // Return success response to React
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Payment successful! Your cake is being prepared. 🎂");
        response.put("orderId", payment.getOrder().getId());
        response.put("paymentId", razorpayPaymentId);
        response.put("amount", payment.getAmount());

        return response;
    }

    // ----------------------------------------------------------
    // Cryptographic signature verification
    // Uses HMAC-SHA256 algorithm
    // ----------------------------------------------------------
    private boolean verifySignature(String razorpayOrderId,
            String razorpayPaymentId, String razorpaySignature) {
        try {
            // The string to hash = orderId + "|" + paymentId
            String data = razorpayOrderId + "|" + razorpayPaymentId;

            // Create HMAC-SHA256 with our secret key
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            // Hash the data
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // Compare our hash with Razorpay's signature
            return hexString.toString().equals(razorpaySignature);

        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }
}