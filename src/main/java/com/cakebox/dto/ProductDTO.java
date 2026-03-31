package com.cakebox.dto;
 
// =============================================================
// FILE: src/main/java/com/cakebox/dto/ProductDTO.java
//
// WHY DTOs INSTEAD OF RETURNING MODEL DIRECTLY?
// Models (Product.java) contain JPA relationships which cause
// infinite JSON loops when serialized:
//   Product → Category → List<Product> → Category → ∞ 💥
//
// DTOs are plain objects with only the data React needs.
// No circular references, no JPA annotations, just clean JSON.
//
// CREATE 3 SEPARATE FILES from this:
//   1. ProductDTO.java         → full product info for product page
//   2. ProductSummaryDTO.java  → shorter version for listing pages
//   3. CategoryDTO.java        → category data for home page
// =============================================================
 
 
// ─────────────────────────────────────────────────────────────
// FILE 1: ProductDTO.java
// Full product details — used on PRODUCT DETAIL PAGE
// ─────────────────────────────────────────────────────────────

 
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
 
@Data
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String mainImageUrl;
    private List<String> imageUrls;
    private List<String> availableFlavors;
    private List<String> availableSizes;
    private Boolean egglessAvailable;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private String occasionTag;
    private Boolean isTrending;
    private Integer stockQuantity;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
 
    // Calculated field: discount percentage shown on product card
    // Example: price=1199, discountPrice=999 → "17% OFF"
    public Integer getDiscountPercentage() {
        if (price == null || discountPrice == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        BigDecimal discount = price.subtract(discountPrice);
        return discount.multiply(BigDecimal.valueOf(100))
                .divide(price, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }
}
 