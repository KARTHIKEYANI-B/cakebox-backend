package com.cakebox.config;

// =============================================================
// FILE: src/main/java/com/cakebox/config/CloudinaryConfig.java
//
// WHAT THIS DOES:
// Connects your Spring Boot app to your Cloudinary account.
// Cloudinary is a cloud service that stores and serves images.
//
// After this config, you can @Autowired Cloudinary anywhere
// and upload images directly to your Cloudinary account.
//
// Your Cloudinary credentials come from application.properties:
//   cloudinary.cloud-name=YOUR_CLOUD_NAME
//   cloudinary.api-key=YOUR_API_KEY
//   cloudinary.api-secret=YOUR_API_SECRET
// =============================================================

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    // ----------------------------------------------------------
    // Creates a Cloudinary bean that Spring manages.
    // Any class can now use:
    //   @Autowired private Cloudinary cloudinary;
    // to upload/delete images.
    // ----------------------------------------------------------
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", "true"); // Always use HTTPS URLs
        return new Cloudinary(config);
    }
}