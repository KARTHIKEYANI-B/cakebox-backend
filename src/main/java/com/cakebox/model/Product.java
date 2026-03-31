package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/Product.java
//
// WHAT THIS FILE DOES:
// Defines the "products" table — your cake listings.
// This is the most important model in your app.
//
// From your UI design, each product has:
//   - Name, description, price, discount price
//   - Star rating
//   - Multiple images
//   - Available flavors (Chocolate, Vanilla, Red Velvet)
//   - Available sizes (0.5kg, 1kg, 2kg)
//   - Egg / Eggless option
//   - Occasion tags (Birthday, Anniversary, etc.)
// =============================================================

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------------------------------------------------------
    // BASIC DETAILS
    // ----------------------------------------------------------
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ----------------------------------------------------------
    // PRICING
    // BigDecimal is used for money (never use double for prices!)
    // Example: price=1499.00, discountPrice=999.00
    // ----------------------------------------------------------
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    // ----------------------------------------------------------
    // IMAGES
    // mainImageUrl   → the big product photo shown on listing page
    // imageUrls      → extra photos shown in product detail carousel
    //
    // @ElementCollection → stores a list of strings in a separate
    // auto-created table called "product_image_urls"
    // ----------------------------------------------------------
    @Column(name = "main_image_url")
    private String mainImageUrl;

    @ElementCollection
    @CollectionTable(name = "product_image_urls",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    // ----------------------------------------------------------
    // AVAILABLE FLAVORS
    // Stored as a list: ["Chocolate", "Vanilla", "Red Velvet"]
    // Shown as clickable chips on the product page
    // ----------------------------------------------------------
    @ElementCollection
    @CollectionTable(name = "product_flavors",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "flavor")
    private List<String> availableFlavors = new ArrayList<>();

    // ----------------------------------------------------------
    // AVAILABLE SIZES
    // Example: ["0.5", "1", "2"] (in kg)
    // ----------------------------------------------------------
    @ElementCollection
    @CollectionTable(name = "product_sizes",
            joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size_kg")
    private List<String> availableSizes = new ArrayList<>();

    // ----------------------------------------------------------
    // EGG / EGGLESS
    // true = product available in eggless version
    // ----------------------------------------------------------
    @Column(name = "eggless_available")
    private Boolean egglessAvailable = true;

    // ----------------------------------------------------------
    // RATINGS
    // averageRating → calculated from all reviews (e.g. 4.6)
    // totalRatings  → how many people rated it
    // ----------------------------------------------------------
    @Column(name = "average_rating", precision = 3, scale = 1)
    private BigDecimal averageRating = BigDecimal.valueOf(0.0);

    @Column(name = "total_ratings")
    private Integer totalRatings = 0;

    // ----------------------------------------------------------
    // OCCASION TAG
    // Links product to occasion-based filtering in your UI.
    // Values: "BIRTHDAY", "ANNIVERSARY", "WEDDING", "FESTIVAL", "ALL"
    // ----------------------------------------------------------
    @Column(name = "occasion_tag")
    private String occasionTag = "ALL";

    // ----------------------------------------------------------
    // TRENDING / POPULAR
    // true = shown in "Popular Near You" section in your UI
    // ----------------------------------------------------------
    @Column(name = "is_trending")
    private Boolean isTrending = false;

    // ----------------------------------------------------------
    // STOCK
    // ----------------------------------------------------------
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 100;

    // ----------------------------------------------------------
    // STATUS
    // false = product hidden from frontend (soft delete)
    // ----------------------------------------------------------
    @Column(name = "is_active")
    private Boolean isActive = true;

    // ----------------------------------------------------------
    // TIMESTAMP
    // ----------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ----------------------------------------------------------
    // RELATIONSHIP: Many Products → One Category
    //
    // @ManyToOne → many products belong to one category
    // @JoinColumn → creates "category_id" column in products table
    // fetch = EAGER → always load category with product
    //                 (we almost always need the category name)
    // ----------------------------------------------------------
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // ----------------------------------------------------------
    // RELATIONSHIP: One Product → Many Reviews
    // ----------------------------------------------------------
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Review> reviews = new ArrayList<>();
}