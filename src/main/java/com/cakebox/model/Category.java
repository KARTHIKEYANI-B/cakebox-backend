package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/Category.java
//
// WHAT THIS FILE DOES:
// Defines the "categories" table.
// Categories from your UI design:
//   → Cakes, Brownies, Sweets, Chocolates, Ice Cream
//   → Also occasion-based: Birthday, Anniversary, Wedding, Festival
// =============================================================

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------------------------------------------------------
    // CATEGORY NAME
    // Examples: "Cakes", "Brownies", "Sweets", "Chocolates"
    // ----------------------------------------------------------
    @Column(nullable = false, unique = true)
    private String name;

    // ----------------------------------------------------------
    // DESCRIPTION
    // Short description shown on the UI card
    // ----------------------------------------------------------
    private String description;

    // ----------------------------------------------------------
    // IMAGE URL
    // Stored in Cloudinary. Example:
    // "https://res.cloudinary.com/cakebox/image/upload/v1/categories/cakes.jpg"
    // ----------------------------------------------------------
    @Column(name = "image_url")
    private String imageUrl;

    // ----------------------------------------------------------
    // OCCASION TAG
    // Links this category to an occasion for the horizontal scroll UI.
    // Values: "BIRTHDAY", "ANNIVERSARY", "WEDDING", "FESTIVAL", "ALL"
    // Example: Red Velvet cake → occasionTag = "ANNIVERSARY"
    // ----------------------------------------------------------
    @Column(name = "occasion_tag")
    private String occasionTag;

    // ----------------------------------------------------------
    // DISPLAY ORDER
    // Controls which category appears first in the UI grid.
    // Lower number = appears first.
    // ----------------------------------------------------------
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // ----------------------------------------------------------
    // IS ACTIVE
    // false = hidden from frontend (soft delete)
    // ----------------------------------------------------------
    @Column(name = "is_active")
    private Boolean isActive = true;

    // ----------------------------------------------------------
    // RELATIONSHIP: One Category → Many Products
    // ----------------------------------------------------------
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
}