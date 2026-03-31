package com.cakebox.controller;

// =============================================================
// FILE: src/main/java/com/cakebox/controller/CustomizationController.java
// Handles image upload for the "Design Your Cake" feature
// =============================================================

import com.cakebox.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/customize")
@RequiredArgsConstructor
public class CustomizationController {

    private final CloudinaryService cloudinaryService;

    // Upload reference image for custom cake
    // React: axios.post('/api/customize/upload-image', formData)
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(
            Authentication auth,
            @RequestParam("image") MultipartFile image) throws IOException {

        String imageUrl = cloudinaryService.uploadImage(image, "custom");
        return ResponseEntity.ok(Map.of(
                "imageUrl", imageUrl,
                "message", "Image uploaded successfully"
        ));
    }
}