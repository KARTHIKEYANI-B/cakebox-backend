package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/CartItem.java
//
// WHAT THIS FILE DOES:
// Each row = one cake item inside a cart.
// Stores all customization choices the user made:
//   - Which flavor (Chocolate / Vanilla / Red Velvet)
//   - Which size (0.5kg / 1kg / 2kg)
//   - Egg or Eggless
//   - Custom message on cake
// =============================================================

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------------------------------------------------------
    // QUANTITY
    // How many of this cake the user wants
    // ----------------------------------------------------------
    @Column(nullable = false)
    private Integer quantity = 1;

    // ----------------------------------------------------------
    // CUSTOMIZATION CHOICES (from your product page UI)
    // ----------------------------------------------------------

    // Which flavor was selected: "Chocolate", "Vanilla", "Red Velvet"
    private String flavor;

    // Which size was selected: "0.5", "1", "2" (in kg)
    @Column(name = "size_kg")
    private String sizeKg;

    // true = eggless version selected
    @Column(name = "is_eggless")
    private Boolean isEggless = false;

    // Message to be written on the cake
    @Column(name = "custom_message")
    private String customMessage;

    // Price at time of adding to cart (price can change later)
    @Column(name = "price_at_add", precision = 10, scale = 2)
    private BigDecimal priceAtAdd;

    // ----------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}