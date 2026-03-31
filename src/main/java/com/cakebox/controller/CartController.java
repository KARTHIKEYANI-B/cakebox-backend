package com.cakebox.controller;

import com.cakebox.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // GET current user's cart
    // React: axios.get('/api/cart')  with Bearer token
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(Authentication auth) {
        return ResponseEntity.ok(cartService.getCart(auth.getName()));
    }

    // ADD item to cart
    // React: axios.post('/api/cart/add', { productId, quantity, flavor, sizeKg... })
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(
            Authentication auth,
            @RequestBody Map<String, Object> request) {

        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = request.containsKey("quantity") ?
                Integer.valueOf(request.get("quantity").toString()) : 1;
        String flavor = (String) request.get("flavor");
        String sizeKg = (String) request.get("sizeKg");
        Boolean isEggless = request.containsKey("isEggless") ?
                Boolean.valueOf(request.get("isEggless").toString()) : false;
        String customMessage = (String) request.get("customMessage");

        return ResponseEntity.ok(cartService.addToCart(
                auth.getName(), productId, quantity,
                flavor, sizeKg, isEggless, customMessage));
    }

    // UPDATE quantity of a cart item
    // React: axios.put('/api/cart/update/5', { quantity: 2 })
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<Map<String, Object>> updateItem(
            Authentication auth,
            @PathVariable Long cartItemId,
            @RequestBody Map<String, Integer> request) {

        Integer quantity = request.get("quantity");
        return ResponseEntity.ok(
                cartService.updateCartItem(auth.getName(), cartItemId, quantity));
    }

    // REMOVE single item from cart
    // React: axios.delete('/api/cart/remove/5')
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<Map<String, Object>> removeItem(
            Authentication auth,
            @PathVariable Long cartItemId) {

        return ResponseEntity.ok(
                cartService.removeFromCart(auth.getName(), cartItemId));
    }

    // CLEAR entire cart
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearCart(Authentication auth) {
        cartService.clearCart(auth.getName());
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}