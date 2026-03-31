package com.cakebox.service;

// =============================================================
// FILE: src/main/java/com/cakebox/service/CloudinaryService.java
//
// WHAT THIS DOES:
// Handles all image operations:
//   1. uploadImage()  → upload a file to Cloudinary, get back URL
//   2. deleteImage()  → delete an image from Cloudinary by URL
//
// WHERE IMAGES COME FROM:
//   - Admin uploads cake product photos (via admin panel)
//   - Customers upload reference photos for custom cakes
//
// HOW CLOUDINARY URLS LOOK:
//   https://res.cloudinary.com/YOUR_CLOUD/image/upload/v123/cakebox/products/abc.jpg
//
// We save this URL in the products.main_image_url column.
// React uses this URL directly in <img src="..."> tags.
// =============================================================

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j  // Lombok: gives us log.info(), log.error() etc
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // ----------------------------------------------------------
    // UPLOAD IMAGE
    //
    // @param file   - the image file from React (multipart/form-data)
    // @param folder - subfolder in Cloudinary ("products", "categories", "custom")
    // @return       - the Cloudinary URL of the uploaded image
    //
    // Example call:
    //   String url = cloudinaryService.uploadImage(file, "products");
    //   product.setMainImageUrl(url); // "https://res.cloudinary.com/..."
    // ----------------------------------------------------------
    public String uploadImage(MultipartFile file, String folder) throws IOException {

        // Validate file type — only allow images
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed (jpg, png, webp)");
        }

        // Validate file size — max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image size must be less than 5MB");
        }

        log.info("Uploading image to Cloudinary folder: cakebox/{}", folder);

        // Upload to Cloudinary with options
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        // Store in cakebox/products/ or cakebox/custom/ etc.
                        "folder", "cakebox/" + folder,

                        // Auto-generate a unique public ID
                        "use_filename", false,
                        "unique_filename", true,

                        // Auto-detect resource type (image/video/raw)
                        "resource_type", "auto",

                        // Optimize quality automatically
                        "quality", "auto",

                        // Convert to WebP for faster loading
                        "fetch_format", "auto"
                )
        );

        // Cloudinary returns a Map with many fields.
        // "secure_url" is the HTTPS URL we save in the database.
        String imageUrl = (String) uploadResult.get("secure_url");
        log.info("Image uploaded successfully: {}", imageUrl);

        return imageUrl;
    }

    // ----------------------------------------------------------
    // DELETE IMAGE
    //
    // Used when:
    //   - Admin deletes a product (remove its image)
    //   - Admin updates a product photo (remove old one)
    //
    // @param imageUrl - the full Cloudinary URL stored in database
    //
    // We extract the "public_id" from the URL to delete it.
    // URL:       https://res.cloudinary.com/cloud/image/upload/v123/cakebox/products/abc123.jpg
    // Public ID: cakebox/products/abc123
    // ----------------------------------------------------------
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        try {
            // Extract public ID from URL
            // Split by "/upload/" → take the part after it
            // Remove the file extension (.jpg, .png, .webp)
            String publicId = extractPublicId(imageUrl);

            log.info("Deleting image from Cloudinary: {}", publicId);

            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

            log.info("Image deleted successfully");
        } catch (Exception e) {
            // Don't crash the app if image deletion fails
            // (image might already be deleted or URL might be external)
            log.warn("Could not delete image from Cloudinary: {}", e.getMessage());
        }
    }

    // ----------------------------------------------------------
    // HELPER: Extract Cloudinary public_id from URL
    //
    // Input:  "https://res.cloudinary.com/mycloud/image/upload/v1234/cakebox/products/cake1.jpg"
    // Output: "cakebox/products/cake1"
    // ----------------------------------------------------------
    private String extractPublicId(String imageUrl) {
        // Find "/upload/" in the URL
        int uploadIndex = imageUrl.indexOf("/upload/");
        if (uploadIndex == -1) return imageUrl;

        // Get everything after "/upload/vXXXXX/"
        String afterUpload = imageUrl.substring(uploadIndex + 8);

        // Remove the version part "v1234/" if present
        if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }

        // Remove file extension (.jpg, .png, .webp)
        int dotIndex = afterUpload.lastIndexOf(".");
        if (dotIndex != -1) {
            afterUpload = afterUpload.substring(0, dotIndex);
        }

        return afterUpload;
    }
}