package com.cakebox.controller;

// =============================================================
// FILE: src/main/java/com/cakebox/controller/CategoryController.java
//
// PUBLIC ENDPOINTS (no login needed):
//   GET  /api/categories              → all active categories
//   GET  /api/categories/{id}         → single category
//   GET  /api/categories/occasion/{tag} → by occasion (BIRTHDAY etc)
//
// ADMIN ENDPOINTS (ROLE_ADMIN required):
//   POST   /api/admin/categories       → create
//   PUT    /api/admin/categories/{id}  → update
//   DELETE /api/admin/categories/{id}  → soft delete
// =============================================================

import com.cakebox.dto.CategoryDTO;
import com.cakebox.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // ── PUBLIC ROUTES ─────────────────────────────────────────

    // GET all active categories
    // React call: axios.get('/api/categories')
    @GetMapping("/api/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllActiveCategories());
    }

    // GET single category by ID
    // React call: axios.get('/api/categories/1')
    @GetMapping("/api/categories/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // GET categories by occasion tag
    // React call: axios.get('/api/categories/occasion/BIRTHDAY')
    // Used for: clicking "Birthday" in the occasion horizontal scroll
    @GetMapping("/api/categories/occasion/{tag}")
    public ResponseEntity<List<CategoryDTO>> getCategoriesByOccasion(
            @PathVariable String tag) {
        return ResponseEntity.ok(categoryService.getCategoriesByOccasion(tag));
    }

    // ── ADMIN ROUTES ──────────────────────────────────────────
    // These use multipart/form-data because they include image uploads

    // CREATE category (admin only)
    // Uses @PreAuthorize to check ROLE_ADMIN (enabled by @EnableMethodSecurity)
    @PostMapping(value = "/api/admin/categories",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String occasionTag,
            @RequestParam(required = false) Integer displayOrder,
            @RequestParam(required = false) MultipartFile image) throws IOException {

        CategoryDTO created = categoryService.createCategory(
                name, description, occasionTag, displayOrder, image);
        return ResponseEntity.status(201).body(created);
    }

    // UPDATE category (admin only)
    @PutMapping(value = "/api/admin/categories/{id}",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String occasionTag,
            @RequestParam(required = false) Integer displayOrder,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) MultipartFile image) throws IOException {

        CategoryDTO updated = categoryService.updateCategory(
                id, name, description, occasionTag, displayOrder, isActive, image);
        return ResponseEntity.ok(updated);
    }

    // DELETE category (admin only)
    @DeleteMapping("/api/admin/categories/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}