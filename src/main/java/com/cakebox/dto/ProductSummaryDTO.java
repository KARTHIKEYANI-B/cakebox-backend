
 
// ─────────────────────────────────────────────────────────────
// FILE 2: ProductSummaryDTO.java
// Short product info — used on HOME PAGE and LISTING PAGES
// Less data = faster API response = faster page load
// ─────────────────────────────────────────────────────────────
package com.cakebox.dto;
 
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
 
@Data
@Builder
public class ProductSummaryDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String mainImageUrl;
    private BigDecimal averageRating;
    private Integer totalRatings;
    private Boolean isTrending;
    private String occasionTag;
    private String categoryName;
}
 