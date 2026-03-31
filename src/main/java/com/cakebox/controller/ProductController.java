package com.cakebox.controller;

// =============================================================
// FILE: src/main/java/com/cakebox/controller/ProductController.java
//
// PUBLIC ENDPOINTS (no login needed):
//   GET /api/products                        → all products
//   GET /api/products/{id}                   → product detail
//   GET /api/products/category/{categoryId}  → by category
//   GET /api/products/occasion/{tag}         → by occasion
//   GET /api/products/trending               → trending products
//   GET /api/products/search?keyword=velvet  → search
//
// ADMIN ENDPOINTS (ROLE_ADMIN required):
//   POST   /api/admin/products       → create product with image
//   PUT    /api/admin/products/{id}  → update product
//   DELETE /api/admin/products/{id}  → soft delete
// =============================================================

import com.cakebox.dto.ProductDTO;
import com.cakebox.dto.ProductSummaryDTO;
import com.cakebox.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ── PUBLIC ROUTES ─────────────────────────────────────────

    // All active products
    // React: axios.get('/api/products')
    @GetMapping("/api/products")
    public ResponseEntity<List<ProductSummaryDTO>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // Single product with full details (for product page)
    // React: axios.get('/api/products/5')
    @GetMapping("/api/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Products filtered by category (for category page)
    // React: axios.get('/api/products/category/1')
    @GetMapping("/api/products/category/{categoryId}")
    public ResponseEntity<List<ProductSummaryDTO>> getByCategory(
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    // Products filtered by occasion
    // React: axios.get('/api/products/occasion/BIRTHDAY')
    // Clicking Birthday in occasion bar → shows birthday cakes
    @GetMapping("/api/products/occasion/{tag}")
    public ResponseEntity<List<ProductSummaryDTO>> getByOccasion(
            @PathVariable String tag) {
        return ResponseEntity.ok(productService.getProductsByOccasion(tag));
    }

    // Trending products for "Popular Near You" section
    // React: axios.get('/api/products/trending')
    @GetMapping("/api/products/trending")
    public ResponseEntity<List<ProductSummaryDTO>> getTrending() {
        return ResponseEntity.ok(productService.getTrendingProducts());
    }

    // Search products by name
    // React: axios.get('/api/products/search?keyword=chocolate')
    @GetMapping("/api/products/search")
    public ResponseEntity<List<ProductSummaryDTO>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    // ── ADMIN ROUTES ──────────────────────────────────────────

    // Create new product with image upload
    // Admin Panel form submits multipart/form-data
    @PostMapping(value = "/api/admin/products",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(required = false) BigDecimal discountPrice,
            @RequestParam(required = false) List<String> flavors,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false, defaultValue = "true") Boolean egglessAvailable,
            @RequestParam(required = false, defaultValue = "ALL") String occasionTag,
            @RequestParam(required = false, defaultValue = "false") Boolean isTrending,
            @RequestParam(required = false, defaultValue = "100") Integer stockQuantity,
            @RequestParam Long categoryId,
            @RequestParam(required = false) MultipartFile mainImage) throws IOException {

        ProductDTO created = productService.createProduct(
                name, description, price, discountPrice,
                flavors, sizes, egglessAvailable, occasionTag,
                isTrending, stockQuantity, categoryId, mainImage);

        return ResponseEntity.status(201).body(created);
    }

    // Update existing product
    @PutMapping(value = "/api/admin/products/{id}",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal price,
            @RequestParam(required = false) BigDecimal discountPrice,
            @RequestParam(required = false) List<String> flavors,
            @RequestParam(required = false) List<String> sizes,
            @RequestParam(required = false) Boolean egglessAvailable,
            @RequestParam(required = false) String occasionTag,
            @RequestParam(required = false) Boolean isTrending,
            @RequestParam(required = false) Integer stockQuantity,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) MultipartFile mainImage) throws IOException {

        ProductDTO updated = productService.updateProduct(
                id, name, description, price, discountPrice,
                flavors, sizes, egglessAvailable, occasionTag,
                isTrending, stockQuantity, isActive, categoryId, mainImage);

        return ResponseEntity.ok(updated);
    }

    // Soft delete product
    @DeleteMapping("/api/admin/products/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Admin: get all products including inactive ones
    @GetMapping("/api/admin/products")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<ProductSummaryDTO>> getAllProductsAdmin() {
        // Returns all (including inactive) for admin panel
        return ResponseEntity.ok(productService.getAllProducts());
    }
}