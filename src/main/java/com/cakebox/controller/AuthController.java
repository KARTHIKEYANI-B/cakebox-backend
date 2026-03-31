package com.cakebox.controller;

// =============================================================
// FILE: src/main/java/com/cakebox/controller/AuthController.java
//
// WHAT THIS FILE DOES:
// Exposes 3 REST API endpoints:
//
//   POST /api/auth/register  → create new account
//   POST /api/auth/login     → login + get JWT token
//   GET  /api/auth/me        → get current user info (needs token)
//
// @RestController = @Controller + @ResponseBody
//   Means: every method returns JSON automatically
//
// @RequestMapping = base URL prefix for all methods in this class
// =============================================================

import com.cakebox.dto.AuthResponse;
import com.cakebox.dto.LoginRequest;
import com.cakebox.dto.RegisterRequest;
import com.cakebox.model.User;
import com.cakebox.repository.UserRepository;
import com.cakebox.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    // ----------------------------------------------------------
    // REGISTER ENDPOINT
    // URL:    POST http://localhost:8080/api/auth/register
    // Body:   { "name": "Ravi", "email": "ravi@gmail.com", "password": "pass123" }
    // Returns: JWT token + user info
    //
    // @Valid → triggers validation annotations on RegisterRequest
    //          (e.g. @NotBlank, @Email, @Size)
    // ResponseEntity → lets us control the HTTP status code
    // HttpStatus.CREATED → 201 Created (correct for new resource)
    // ----------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // Email already exists or other business error
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // ----------------------------------------------------------
    // LOGIN ENDPOINT
    // URL:    POST http://localhost:8080/api/auth/login
    // Body:   { "email": "ravi@gmail.com", "password": "pass123" }
    // Returns: JWT token + user info
    //
    // If wrong credentials → Spring Security automatically returns:
    // { "error": 401, "message": "Bad credentials" }
    // ----------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response); // 200 OK
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password. Please try again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    // ----------------------------------------------------------
    // GET CURRENT USER INFO
    // URL:    GET http://localhost:8080/api/auth/me
    // Header: Authorization: Bearer <your_jwt_token>
    // Returns: Current logged-in user's details
    //
    // Use this in React to:
    //   - Show user's name in Navbar after login
    //   - Check if user is ADMIN to show admin panel
    // ----------------------------------------------------------
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        // Get the email of the currently authenticated user
        // (set by JwtAuthenticationFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return safe user info (never return password!)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("name", user.getName());
        userInfo.put("email", user.getEmail());
        userInfo.put("phoneNumber", user.getPhoneNumber());
        userInfo.put("role", user.getRole());
        userInfo.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(userInfo);
    }

    // ----------------------------------------------------------
    // HEALTH CHECK (useful for testing and Railway deployment)
    // URL: GET http://localhost:8080/api/auth/health
    // No auth needed. Returns "CakeBox API is running!"
    // ----------------------------------------------------------
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "CakeBox API is running! 🎂");
        return ResponseEntity.ok(status);
    }
}