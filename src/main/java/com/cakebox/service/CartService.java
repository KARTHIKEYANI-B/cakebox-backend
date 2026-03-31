package com.cakebox.service;

import com.cakebox.model.*;
import com.cakebox.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ----------------------------------------------------------
    // GET OR CREATE CART for a user
    // Every user has exactly ONE cart. If they don't have one yet
    // (first time visiting cart), we create it automatically.
    // ----------------------------------------------------------
    public Map<String, Object> getCart(String email) {
        User user = getUser(email);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewCart(user));
        return buildCartResponse(cart);
    }

    // ----------------------------------------------------------
    // ADD ITEM TO CART
    // If the same product+flavor+size already exists in cart,
    // we increase the quantity instead of adding a duplicate.
    // ----------------------------------------------------------
    public Map<String, Object> addToCart(String email, Long productId,
            Integer quantity, String flavor, String sizeKg,
            Boolean isEggless, String customMessage) {

        User user = getUser(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getIsActive()) {
            throw new RuntimeException("This product is currently unavailable");
        }
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Only " + product.getStockQuantity() + " items left in stock");
        }

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createNewCart(user));

        // Check if same item (same product + flavor + size + eggless) exists
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item ->
                    item.getProduct().getId().equals(productId) &&
                    Objects.equals(item.getFlavor(), flavor) &&
                    Objects.equals(item.getSizeKg(), sizeKg) &&
                    Objects.equals(item.getIsEggless(), isEggless))
                .findFirst();

        if (existing.isPresent()) {
            // Increase quantity
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Add new cart item
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity != null ? quantity : 1);
            newItem.setFlavor(flavor);
            newItem.setSizeKg(sizeKg != null ? sizeKg : "1");
            newItem.setIsEggless(isEggless != null ? isEggless : false);
            newItem.setCustomMessage(customMessage);
            // Save price at time of adding (price may change later)
            newItem.setPriceAtAdd(
                product.getDiscountPrice() != null ?
                product.getDiscountPrice() : product.getPrice());
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cartRepository.save(cart);
        return buildCartResponse(cart);
    }

    // ----------------------------------------------------------
    // UPDATE CART ITEM QUANTITY
    // quantity = 0 → removes the item entirely
    // ----------------------------------------------------------
    public Map<String, Object> updateCartItem(String email, Long cartItemId, Integer quantity) {
        User user = getUser(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Security: ensure this item belongs to this user's cart
        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to cart item");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow();
        return buildCartResponse(cart);
    }

    // ----------------------------------------------------------
    // REMOVE SINGLE ITEM FROM CART
    // ----------------------------------------------------------
    public Map<String, Object> removeFromCart(String email, Long cartItemId) {
        User user = getUser(email);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        cartItemRepository.delete(item);
        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow();
        return buildCartResponse(cart);
    }

    // ----------------------------------------------------------
    // CLEAR ENTIRE CART
    // Called after order is successfully placed
    // ----------------------------------------------------------
    public void clearCart(String email) {
        User user = getUser(email);
        cartRepository.findByUserId(user.getId()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    // ----------------------------------------------------------
    // HELPERS
    // ----------------------------------------------------------

    private Cart createNewCart(User user) {
        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Builds the full cart response with totals
    private Map<String, Object> buildCartResponse(Cart cart) {
        // Reload fresh cart from DB
        Cart freshCart = cartRepository.findById(cart.getId()).orElse(cart);

        List<Map<String, Object>> items = freshCart.getItems().stream()
                .map(this::buildItemMap)
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(i -> (BigDecimal) i.get("itemTotal"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Free delivery over ₹500
        BigDecimal deliveryCharge = subtotal.compareTo(new BigDecimal("500")) > 0
                ? BigDecimal.ZERO : new BigDecimal("49");

        BigDecimal total = subtotal.add(deliveryCharge);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("cartId", freshCart.getId());
        response.put("items", items);
        response.put("itemCount", items.size());
        response.put("subtotal", subtotal);
        response.put("deliveryCharge", deliveryCharge);
        response.put("total", total);
        response.put("freeDeliveryEligible", deliveryCharge.equals(BigDecimal.ZERO));

        return response;
    }

    private Map<String, Object> buildItemMap(CartItem item) {
        BigDecimal unitPrice = item.getPriceAtAdd() != null ?
                item.getPriceAtAdd() :
                (item.getProduct().getDiscountPrice() != null ?
                 item.getProduct().getDiscountPrice() :
                 item.getProduct().getPrice());

        BigDecimal itemTotal = unitPrice.multiply(new BigDecimal(item.getQuantity()));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("cartItemId", item.getId());
        map.put("productId", item.getProduct().getId());
        map.put("productName", item.getProduct().getName());
        map.put("productImage", item.getProduct().getMainImageUrl());
        map.put("quantity", item.getQuantity());
        map.put("flavor", item.getFlavor());
        map.put("sizeKg", item.getSizeKg());
        map.put("isEggless", item.getIsEggless());
        map.put("customMessage", item.getCustomMessage());
        map.put("unitPrice", unitPrice);
        map.put("itemTotal", itemTotal);
        return map;
    }
}