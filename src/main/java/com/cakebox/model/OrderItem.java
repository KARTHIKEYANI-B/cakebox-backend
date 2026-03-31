package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/OrderItem.java
//
// WHAT THIS FILE DOES:
// One row = one cake inside a placed order.
// We copy product details here (not just a reference) because:
//   - Product name/price might change in the future
//   - Order history must always show ORIGINAL price paid
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
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------------------------------------------------------
    // SNAPSHOT OF PRODUCT AT TIME OF ORDER
    // We store name + price directly (not just FK to product)
    // so historical orders are always accurate.
    // ----------------------------------------------------------
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_image_url")
    private String productImageUrl;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // ----------------------------------------------------------
    // CUSTOMIZATION DETAILS (copied from CartItem)
    // ----------------------------------------------------------
    private String flavor;

    @Column(name = "size_kg")
    private String sizeKg;

    @Column(name = "is_eggless")
    private Boolean isEggless = false;

    @Column(name = "custom_message")
    private String customMessage;

    // ----------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Keep a reference to original product (for review linking)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;

    // Link to customization details (if user did full customization)
    @OneToOne(mappedBy = "orderItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Customization customization;
}