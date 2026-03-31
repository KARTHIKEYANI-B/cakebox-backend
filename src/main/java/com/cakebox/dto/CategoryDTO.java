 
// ─────────────────────────────────────────────────────────────
// FILE 3: CategoryDTO.java
// Used on HOME PAGE category grid and occasion bar
// ─────────────────────────────────────────────────────────────
package com.cakebox.dto;
 
import lombok.Builder;
import lombok.Data;
 
@Data
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String occasionTag;
    private Integer displayOrder;
    private Integer productCount;  // "24 cakes" shown under category
}