package com.cakebox.service;

import com.cakebox.model.*;
import com.cakebox.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;

    // ----------------------------------------------------------
    // PLACE ORDER
    // Converts the user's cart into a permanent order record.
    // Steps:
    //   1. Load user's cart
    //   2. Build Order + OrderItems from CartItems
    //   3. Save order with status ORDER_PLACED
    //   4. Clear the cart
    //   5. Return order summary
    // ----------------------------------------------------------
    public Map<String, Object> placeOrder(String email, Long addressId,
            LocalDate deliveryDate, String deliverySlot,
            Boolean isExpressDelivery, String couponCode,
            Boolean isSurpriseMode, String surpriseMessage,
            Boolean doNotCallRecipient) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Load delivery address
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Ensure address belongs to this user
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Address does not belong to this user");
        }

        // Load cart
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cannot place order — cart is empty");
        }

        // Build Order
        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(address);
        order.setDeliveryDate(deliveryDate);
        order.setDeliverySlot(deliverySlot);
        order.setIsExpressDelivery(isExpressDelivery != null ? isExpressDelivery : false);
        order.setCouponCode(couponCode);
        order.setIsSurpriseMode(isSurpriseMode != null ? isSurpriseMode : false);
        order.setSurpriseMessage(surpriseMessage);
        order.setDoNotCallRecipient(doNotCallRecipient != null ? doNotCallRecipient : false);
        order.setStatus(Order.OrderStatus.ORDER_PLACED);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        // Convert CartItems → OrderItems
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(cartItem.getProduct());

            // Snapshot product details (price may change in future)
            oi.setProductName(cartItem.getProduct().getName());
            oi.setProductImageUrl(cartItem.getProduct().getMainImageUrl());
            oi.setQuantity(cartItem.getQuantity());

            BigDecimal unitPrice = cartItem.getPriceAtAdd() != null ?
                    cartItem.getPriceAtAdd() :
                    (cartItem.getProduct().getDiscountPrice() != null ?
                     cartItem.getProduct().getDiscountPrice() :
                     cartItem.getProduct().getPrice());

            oi.setUnitPrice(unitPrice);
            oi.setTotalPrice(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            oi.setFlavor(cartItem.getFlavor());
            oi.setSizeKg(cartItem.getSizeKg());
            oi.setIsEggless(cartItem.getIsEggless());
            oi.setCustomMessage(cartItem.getCustomMessage());

            orderItems.add(oi);
            subtotal = subtotal.add(oi.getTotalPrice());
        }

        // Delivery charge: free over ₹500
        BigDecimal deliveryCharge = subtotal.compareTo(new BigDecimal("500")) > 0
                ? BigDecimal.ZERO : new BigDecimal("49");

        // Express delivery adds ₹99
        if (Boolean.TRUE.equals(isExpressDelivery)) {
            deliveryCharge = deliveryCharge.add(new BigDecimal("99"));
        }

        BigDecimal total = subtotal.add(deliveryCharge);

        order.setSubtotal(subtotal);
        order.setDeliveryCharge(deliveryCharge);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Clear cart after order placed
        cartService.clearCart(email);

        log.info("Order placed: #{} by {} — ₹{}", savedOrder.getId(), email, total);

        return buildOrderResponse(savedOrder);
    }

    // ----------------------------------------------------------
    // GET ORDER HISTORY for a user
    // ----------------------------------------------------------
    public List<Map<String, Object>> getMyOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::buildOrderSummary)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // GET SINGLE ORDER DETAIL + TRACKING STATUS
    // Powers the live order tracking page in your UI
    // ----------------------------------------------------------
    public Map<String, Object> getOrderById(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Security: only owner or admin can see the order
        if (!order.getUser().getId().equals(user.getId())
                && !user.getRole().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Unauthorized to view this order");
        }

        return buildOrderResponse(order);
    }

    // ----------------------------------------------------------
    // ADMIN: UPDATE ORDER STATUS
    // Shop owner changes: ORDER_PLACED → BAKING → OUT_FOR_DELIVERY → DELIVERED
    // ----------------------------------------------------------
    public Map<String, Object> updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status. Valid values: " +
                "ORDER_PLACED, CONFIRMED, BAKING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED");
        }

        orderRepository.save(order);
        log.info("Order #{} status updated to {}", orderId, status);
        return buildOrderResponse(order);
    }

    // ----------------------------------------------------------
    // ADMIN: GET ALL ORDERS
    // ----------------------------------------------------------
    public List<Map<String, Object>> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::buildOrderSummary)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // Called by PaymentService after payment is verified
    // ----------------------------------------------------------
    public void markOrderAsPaid(Long orderId, String razorpayOrderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setPaymentStatus(Order.PaymentStatus.PAID);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        order.setRazorpayOrderId(razorpayOrderId);
        orderRepository.save(order);
        log.info("Order #{} marked as PAID", orderId);
    }

    // ----------------------------------------------------------
    // ADD DELIVERY ADDRESS
    // ----------------------------------------------------------
    public Map<String, Object> addAddress(String email, String fullName,
            String phoneNumber, String addressLine1, String addressLine2,
            String city, String state, String pincode, Boolean isDefault) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If this is default, unset other defaults
        if (Boolean.TRUE.equals(isDefault)) {
            addressRepository.findByUserId(user.getId())
                    .forEach(a -> { a.setIsDefault(false); addressRepository.save(a); });
        }

        Address address = new Address();
        address.setUser(user);
        address.setFullName(fullName);
        address.setPhoneNumber(phoneNumber);
        address.setAddressLine1(addressLine1);
        address.setAddressLine2(addressLine2);
        address.setCity(city);
        address.setState(state);
        address.setPincode(pincode);
        address.setIsDefault(isDefault != null ? isDefault : false);

        Address saved = addressRepository.save(address);

        return Map.of(
            "addressId", saved.getId(),
            "fullName", saved.getFullName(),
            "city", saved.getCity(),
            "pincode", saved.getPincode(),
            "isDefault", saved.getIsDefault(),
            "message", "Address saved successfully"
        );
    }

    // ----------------------------------------------------------
    // GET USER ADDRESSES
    // ----------------------------------------------------------
    public List<Map<String, Object>> getMyAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return addressRepository.findByUserId(user.getId())
                .stream()
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("addressId", a.getId());
                    m.put("fullName", a.getFullName());
                    m.put("phoneNumber", a.getPhoneNumber());
                    m.put("addressLine1", a.getAddressLine1());
                    m.put("addressLine2", a.getAddressLine2());
                    m.put("city", a.getCity());
                    m.put("state", a.getState());
                    m.put("pincode", a.getPincode());
                    m.put("isDefault", a.getIsDefault());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // HELPERS: Build response maps
    // ----------------------------------------------------------
    private Map<String, Object> buildOrderResponse(Order order) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("orderId", order.getId());
        resp.put("status", order.getStatus().name());
        resp.put("paymentStatus", order.getPaymentStatus().name());
        resp.put("subtotal", order.getSubtotal());
        resp.put("deliveryCharge", order.getDeliveryCharge());
        resp.put("totalAmount", order.getTotalAmount());
        resp.put("deliveryDate", order.getDeliveryDate());
        resp.put("deliverySlot", order.getDeliverySlot());
        resp.put("isExpressDelivery", order.getIsExpressDelivery());
        resp.put("isSurpriseMode", order.getIsSurpriseMode());
        resp.put("createdAt", order.getCreatedAt());
        resp.put("razorpayOrderId", order.getRazorpayOrderId());

        // Order tracking steps for UI
        resp.put("trackingSteps", buildTrackingSteps(order.getStatus()));

        // Items
        List<Map<String, Object>> items = order.getItems().stream()
                .map(oi -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("orderItemId", oi.getId());
                    m.put("productName", oi.getProductName());
                    m.put("productImage", oi.getProductImageUrl());
                    m.put("quantity", oi.getQuantity());
                    m.put("unitPrice", oi.getUnitPrice());
                    m.put("totalPrice", oi.getTotalPrice());
                    m.put("flavor", oi.getFlavor());
                    m.put("sizeKg", oi.getSizeKg());
                    m.put("isEggless", oi.getIsEggless());
                    m.put("customMessage", oi.getCustomMessage());
                    return m;
                }).collect(Collectors.toList());
        resp.put("items", items);

        // Delivery address
        if (order.getDeliveryAddress() != null) {
            Address a = order.getDeliveryAddress();
            resp.put("deliveryAddress", Map.of(
                "fullName", a.getFullName(),
                "addressLine1", a.getAddressLine1(),
                "city", a.getCity(),
                "pincode", a.getPincode(),
                "phoneNumber", a.getPhoneNumber()
            ));
        }

        return resp;
    }

    // Builds the live tracking steps array for frontend UI
    // 🍰 Order Placed → ✅ Confirmed → 🔥 Baking → 🚚 Out for Delivery → 🎉 Delivered
    private List<Map<String, Object>> buildTrackingSteps(Order.OrderStatus currentStatus) {
        List<String> statuses = Arrays.asList(
            "ORDER_PLACED", "CONFIRMED", "BAKING", "OUT_FOR_DELIVERY", "DELIVERED"
        );
        List<String> labels = Arrays.asList(
            "Order Placed", "Confirmed", "Baking", "Out for Delivery", "Delivered"
        );
        List<String> icons = Arrays.asList("📦", "✅", "🍰", "🚚", "🎉");

        int currentIndex = statuses.indexOf(currentStatus.name());

        List<Map<String, Object>> steps = new ArrayList<>();
        for (int i = 0; i < statuses.size(); i++) {
            steps.add(Map.of(
                "step", statuses.get(i),
                "label", labels.get(i),
                "icon", icons.get(i),
                "completed", i <= currentIndex,
                "active", i == currentIndex
            ));
        }
        return steps;
    }

    private Map<String, Object> buildOrderSummary(Order order) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId", order.getId());
        m.put("status", order.getStatus().name());
        m.put("paymentStatus", order.getPaymentStatus().name());
        m.put("totalAmount", order.getTotalAmount());
        m.put("itemCount", order.getItems().size());
        m.put("deliveryDate", order.getDeliveryDate());
        m.put("createdAt", order.getCreatedAt());
        // First item image for thumbnail in order history
        order.getItems().stream().findFirst().ifPresent(oi -> {
            m.put("firstItemName", oi.getProductName());
            m.put("firstItemImage", oi.getProductImageUrl());
        });
        return m;
    }
}