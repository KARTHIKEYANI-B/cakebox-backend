package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/Cart.java
//
// WHAT THIS FILE DOES:
// Each user has ONE active cart.
// The cart contains multiple CartItems.
//
// Think of it like:
//   Cart (the basket) → CartItems (the cakes inside the basket)
// =============================================================

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ----------------------------------------------------------
    // RELATIONSHIP: One Cart → One User
    // ----------------------------------------------------------
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ----------------------------------------------------------
    // RELATIONSHIP: One Cart → Many CartItems
    // orphanRemoval = true → if CartItem is removed from this list,
    // it's automatically deleted from the DB too
    // ----------------------------------------------------------
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();
}