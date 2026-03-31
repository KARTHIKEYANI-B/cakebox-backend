package com.cakebox.service;

// =============================================================
// FILE: src/main/java/com/cakebox/service/CategoryService.java
//
// WHAT THIS DOES:
// All business logic for categories:
//   - Get all active categories (home page grid)
//   - Get by occasion tag (Birthday, Anniversary, etc.)
//   - Admin: create, update, delete categories
// =============================================================

import com.cakebox.dto.CategoryDTO;
import com.cakebox.model.Category;
import com.cakebox.repository.CategoryRepository;
import com.cakebox.repository.ProductRepository;
import com.cakebox.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    // ----------------------------------------------------------
    // GET ALL ACTIVE CATEGORIES
    // Used on: Home page category grid, product filter sidebar
    // Returns: List of active categories sorted by displayOrder
    // ----------------------------------------------------------
    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository
                .findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // GET CATEGORIES BY OCCASION
    // Used on: Occasion-based horizontal scroll (Birthday, Wedding...)
    // Example: getAllByOccasion("BIRTHDAY") → all birthday categories
    // ----------------------------------------------------------
    public List<CategoryDTO> getCategoriesByOccasion(String occasion) {
        return categoryRepository
                .findByOccasionTag(occasion.toUpperCase())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------
    // GET SINGLE CATEGORY
    // ----------------------------------------------------------
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toDTO(category);
    }

    // ----------------------------------------------------------
    // ADMIN: CREATE CATEGORY
    // Called from Admin Panel when owner adds a new category
    // ----------------------------------------------------------
    public CategoryDTO createCategory(String name, String description,
                                       String occasionTag, Integer displayOrder,
                                       MultipartFile imageFile) throws IOException {
        // Check if category name already exists
        if (categoryRepository.findAll().stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name))) {
            throw new RuntimeException("Category with name '" + name + "' already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setOccasionTag(occasionTag != null ? occasionTag.toUpperCase() : "ALL");
        category.setDisplayOrder(displayOrder != null ? displayOrder : 99);
        category.setIsActive(true);

        // Upload image to Cloudinary if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(imageFile, "categories");
            category.setImageUrl(imageUrl);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created: {}", saved.getName());
        return toDTO(saved);
    }

    // ----------------------------------------------------------
    // ADMIN: UPDATE CATEGORY
    // ----------------------------------------------------------
    public CategoryDTO updateCategory(Long id, String name, String description,
                                       String occasionTag, Integer displayOrder,
                                       Boolean isActive, MultipartFile imageFile) throws IOException {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        if (name != null) category.setName(name);
        if (description != null) category.setDescription(description);
        if (occasionTag != null) category.setOccasionTag(occasionTag.toUpperCase());
        if (displayOrder != null) category.setDisplayOrder(displayOrder);
        if (isActive != null) category.setIsActive(isActive);

        // If a new image is uploaded, replace the old one
        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image from Cloudinary
            if (category.getImageUrl() != null) {
                cloudinaryService.deleteImage(category.getImageUrl());
            }
            // Upload new image
            String newImageUrl = cloudinaryService.uploadImage(imageFile, "categories");
            category.setImageUrl(newImageUrl);
        }

        Category updated = categoryRepository.save(category);
        log.info("Category updated: {}", updated.getName());
        return toDTO(updated);
    }

    // ----------------------------------------------------------
    // ADMIN: DELETE CATEGORY (soft delete)
    // Sets isActive = false instead of actually deleting.
    // This preserves historical order data.
    // ----------------------------------------------------------
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        category.setIsActive(false);
        categoryRepository.save(category);
        log.info("Category soft-deleted: {}", category.getName());
    }

    // ----------------------------------------------------------
    // HELPER: Convert Category model → CategoryDTO
    // This is called a "mapper" — converts DB model to API response
    // ----------------------------------------------------------
    private CategoryDTO toDTO(Category category) {
        // Count how many active products are in this category
        long productCount = productRepository
                .findByCategoryIdAndIsActiveTrue(category.getId()).size();

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .occasionTag(category.getOccasionTag())
                .displayOrder(category.getDisplayOrder())
                .productCount((int) productCount)
                .build();
    }
}
