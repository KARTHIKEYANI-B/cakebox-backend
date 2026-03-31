package com.cakebox.service;

// =============================================================
// FILE: src/main/java/com/cakebox/service/ProductService.java
//
// WHAT THIS DOES:
// All business logic for products (cakes):
//   - Get all products (home page listing)
//   - Get by category (Cakes, Brownies, etc.)
//   - Get by occasion (Birthday, Anniversary, etc.)
//   - Get trending products ("Popular Near You" section)
//   - Search by name (search bar)
//   - Get single product with full details
//   - Admin: create, update, delete products with image upload
//   - Update rating when a review is added
// =============================================================

import com.cakebox.dto.ProductDTO;
import com.cakebox.dto.ProductSummaryDTO;
import com.cakebox.model.Category;
import com.cakebox.model.Product;
import com.cakebox.repository.CategoryRepository;
import com.cakebox.repository.ProductRepository;
import com.cakebox.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final CloudinaryService cloudinaryService;

    // ----------------------------------------------------------
    // GET ALL ACTIVE PRODUCTS
    // Used on: Product listing page
    // ----------------------------------------------------------
    public List<ProductSummaryDTO> getAllProducts() {
        return productRepository.findByIsActiveTrue()
                .stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // GET PRODUCTS BY CATEGORY
    // Used when user clicks a category card on home page
    // Example: categoryId=1 → all active cakes in "Cakes" category
    // ----------------------------------------------------------
    public List<ProductSummaryDTO> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId)
                .stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // GET PRODUCTS BY OCCASION
    // Used when user clicks Birthday / Anniversary / Wedding / Festival
    // from the horizontal occasion bar on home page
    // ----------------------------------------------------------
    public List<ProductSummaryDTO> getProductsByOccasion(String occasion) {
        return productRepository.findByOccasionTagAndIsActiveTrue(occasion.toUpperCase())
                .stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // GET TRENDING PRODUCTS
    // Used on: "Popular Near You" section on home page
    // Admin sets isTrending=true on products they want featured
    // ----------------------------------------------------------
    public List<ProductSummaryDTO> getTrendingProducts() {
        return productRepository.findByIsTrendingTrueAndIsActiveTrue()
                .stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // SEARCH PRODUCTS BY NAME
    // Used on: Search bar in Navbar
    // Example: "red velvet" → finds "Red Velvet Bliss", "Red Velvet Truffle"
    // Case-insensitive search
    // ----------------------------------------------------------
    public List<ProductSummaryDTO> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchByName(keyword.trim())
                .stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // GET SINGLE PRODUCT — Full details
    // Used on: Product detail page
    // Returns full ProductDTO with all images, flavors, sizes, etc.
    // ----------------------------------------------------------
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (!product.getIsActive()) {
            throw new RuntimeException("This product is currently unavailable");
        }

        return toDTO(product);
    }

    // ----------------------------------------------------------
    // ADMIN: CREATE PRODUCT
    // Admin fills a form → React sends multipart/form-data
    // (text fields + image file together)
    // ----------------------------------------------------------
    public ProductDTO createProduct(
            String name, String description,
            BigDecimal price, BigDecimal discountPrice,
            List<String> flavors, List<String> sizes,
            Boolean egglessAvailable, String occasionTag,
            Boolean isTrending, Integer stockQuantity,
            Long categoryId, MultipartFile mainImage) throws IOException {

        // Validate category exists
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setDiscountPrice(discountPrice);
        product.setAvailableFlavors(flavors != null ? flavors :
                Arrays.asList("Chocolate", "Vanilla", "Red Velvet"));
        product.setAvailableSizes(sizes != null ? sizes :
                Arrays.asList("0.5", "1", "2"));
        product.setEgglessAvailable(egglessAvailable != null ? egglessAvailable : true);
        product.setOccasionTag(occasionTag != null ? occasionTag.toUpperCase() : "ALL");
        product.setIsTrending(isTrending != null ? isTrending : false);
        product.setStockQuantity(stockQuantity != null ? stockQuantity : 100);
        product.setCategory(category);
        product.setIsActive(true);
        product.setAverageRating(BigDecimal.ZERO);
        product.setTotalRatings(0);

        // Upload main image to Cloudinary
        if (mainImage != null && !mainImage.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(mainImage, "products");
            product.setMainImageUrl(imageUrl);
        }

        Product saved = productRepository.save(product);
        log.info("Product created: {} (id: {})", saved.getName(), saved.getId());
        return toDTO(saved);
    }

    // ----------------------------------------------------------
    // ADMIN: UPDATE PRODUCT
    // Only update fields that are provided (null = no change)
    // ----------------------------------------------------------
    public ProductDTO updateProduct(
            Long id, String name, String description,
            BigDecimal price, BigDecimal discountPrice,
            List<String> flavors, List<String> sizes,
            Boolean egglessAvailable, String occasionTag,
            Boolean isTrending, Integer stockQuantity,
            Boolean isActive, Long categoryId,
            MultipartFile mainImage) throws IOException {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (name != null) product.setName(name);
        if (description != null) product.setDescription(description);
        if (price != null) product.setPrice(price);
        if (discountPrice != null) product.setDiscountPrice(discountPrice);
        if (flavors != null) product.setAvailableFlavors(flavors);
        if (sizes != null) product.setAvailableSizes(sizes);
        if (egglessAvailable != null) product.setEgglessAvailable(egglessAvailable);
        if (occasionTag != null) product.setOccasionTag(occasionTag.toUpperCase());
        if (isTrending != null) product.setIsTrending(isTrending);
        if (stockQuantity != null) product.setStockQuantity(stockQuantity);
        if (isActive != null) product.setIsActive(isActive);

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Replace image if new one uploaded
        if (mainImage != null && !mainImage.isEmpty()) {
            if (product.getMainImageUrl() != null) {
                cloudinaryService.deleteImage(product.getMainImageUrl());
            }
            String newUrl = cloudinaryService.uploadImage(mainImage, "products");
            product.setMainImageUrl(newUrl);
        }

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getName());
        return toDTO(updated);
    }

    // ----------------------------------------------------------
    // ADMIN: DELETE PRODUCT (soft delete)
    // ----------------------------------------------------------
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: {}", product.getName());
    }

    // ----------------------------------------------------------
    // CALLED BY ReviewService when a new review is submitted
    // Recalculates the product's average rating and total count
    // ----------------------------------------------------------
    public void recalculateRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Double avg = reviewRepository.findAverageRatingByProductId(productId);
        Long count = reviewRepository.countByProductId(productId);

        product.setAverageRating(avg != null ?
                BigDecimal.valueOf(avg).setScale(1, java.math.RoundingMode.HALF_UP) :
                BigDecimal.ZERO);
        product.setTotalRatings(count != null ? count.intValue() : 0);

        productRepository.save(product);
        log.info("Rating recalculated for product {}: {} ({} reviews)",
                productId, product.getAverageRating(), product.getTotalRatings());
    }

    // ----------------------------------------------------------
    // MAPPERS: Model → DTO conversion
    // ----------------------------------------------------------

    // Summary DTO (for listing pages — less data, faster)
    public ProductSummaryDTO toSummaryDTO(Product product) {
        return ProductSummaryDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .mainImageUrl(product.getMainImageUrl())
                .averageRating(product.getAverageRating())
                .totalRatings(product.getTotalRatings())
                .isTrending(product.getIsTrending())
                .occasionTag(product.getOccasionTag())
                .categoryName(product.getCategory() != null ?
                        product.getCategory().getName() : null)
                .build();
    }

    // Full DTO (for product detail page — all data)
    public ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .mainImageUrl(product.getMainImageUrl())
                .imageUrls(product.getImageUrls())
                .availableFlavors(product.getAvailableFlavors())
                .availableSizes(product.getAvailableSizes())
                .egglessAvailable(product.getEgglessAvailable())
                .averageRating(product.getAverageRating())
                .totalRatings(product.getTotalRatings())
                .occasionTag(product.getOccasionTag())
                .isTrending(product.getIsTrending())
                .stockQuantity(product.getStockQuantity())
                .categoryId(product.getCategory() != null ?
                        product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ?
                        product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
