package com.cakebox.controller;

import com.cakebox.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ── USER ROUTES ────────────────────────────────────────────

    // PLACE ORDER from current cart
    @PostMapping("/api/orders/place")
    public ResponseEntity<Map<String, Object>> placeOrder(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        Long addressId = Long.valueOf(request.get("addressId").toString());
        LocalDate deliveryDate = request.containsKey("deliveryDate") ?
                LocalDate.parse(request.get("deliveryDate").toString()) : LocalDate.now().plusDays(1);
        String deliverySlot = (String) request.getOrDefault("deliverySlot", "10:00 AM - 12:00 PM");
        Boolean isExpress = (Boolean) request.getOrDefault("isExpressDelivery", false);
        String coupon = (String) request.get("couponCode");
        Boolean surprise = (Boolean) request.getOrDefault("isSurpriseMode", false);
        String surpriseMsg = (String) request.get("surpriseMessage");
        Boolean doNotCall = (Boolean) request.getOrDefault("doNotCallRecipient", false);

        Map<String, Object> order = orderService.placeOrder(
                auth.getName(), addressId, deliveryDate, deliverySlot,
                isExpress, coupon, surprise, surpriseMsg, doNotCall);

        return ResponseEntity.status(201).body(order);
    }

    // GET my order history
    @GetMapping("/api/orders/my")
    public ResponseEntity<List<Map<String, Object>>> getMyOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrders(auth.getName()));
    }

    // GET single order + live tracking
    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(
            Authentication auth, @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(auth.getName(), orderId));
    }

    // ── ADDRESS ROUTES ─────────────────────────────────────────

    // GET my saved addresses
    @GetMapping("/api/orders/addresses")
    public ResponseEntity<List<Map<String, Object>>> getAddresses(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyAddresses(auth.getName()));
    }

    // ADD new delivery address
    @PostMapping("/api/orders/addresses")
    public ResponseEntity<Map<String, Object>> addAddress(
            Authentication auth,
            @RequestBody Map<String, Object> req) {

        return ResponseEntity.status(201).body(orderService.addAddress(
                auth.getName(),
                (String) req.get("fullName"),
                (String) req.get("phoneNumber"),
                (String) req.get("addressLine1"),
                (String) req.getOrDefault("addressLine2", ""),
                (String) req.get("city"),
                (String) req.get("state"),
                (String) req.get("pincode"),
                (Boolean) req.getOrDefault("isDefault", false)
        ));
    }

    // ── ADMIN ROUTES ───────────────────────────────────────────

    // Admin: view all orders
    @GetMapping("/api/admin/orders")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // Admin: update order status (BAKING, OUT_FOR_DELIVERY, etc.)
    @PutMapping("/api/admin/orders/{orderId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(
                orderService.updateOrderStatus(orderId, request.get("status")));
    }
}