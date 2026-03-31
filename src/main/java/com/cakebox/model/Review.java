package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/Review.java
//
// WHAT THIS FILE DOES:
// Customer reviews for products.
// From your UI: "Delivered in 1 hour!" type testimonials shown
// at the bottom of home page and on product pages.
// =============================================================

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"user_id", "product_id"},
           name = "one_review_per_user_per_product"
       ))
// uniqueConstraints → one user can only review each product once
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------------------------------------------------------
    // RATING: 1 to 5 stars
    // @Min, @Max → validated before saving
    // ----------------------------------------------------------
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    @Column(nullable = false)
    private Integer rating;

    // Review text: "Cake was delicious! Delivered in 1 hour!"
    @Column(columnDefinition = "TEXT")
    private String comment;

    // ----------------------------------------------------------
    // TIMESTAMP
    // ----------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ----------------------------------------------------------
    // RELATIONSHIPS
    // ----------------------------------------------------------
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}