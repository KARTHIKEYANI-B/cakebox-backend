package com.cakebox.service;

// =============================================================
// FILE: src/main/java/com/cakebox/service/AuthService.java
//
// WHAT THIS FILE DOES:
// Contains the BUSINESS LOGIC for:
//   1. register() → create new user account
//   2. login()    → verify credentials → return JWT token
//
// Services sit between Controllers and Repositories:
//   Controller (receives HTTP request)
//       ↓
//   Service (business logic — this file)
//       ↓
//   Repository (database query)
// =============================================================

import com.cakebox.dto.AuthResponse;
import com.cakebox.dto.LoginRequest;
import com.cakebox.dto.RegisterRequest;
import com.cakebox.model.User;
import com.cakebox.repository.UserRepository;
import com.cakebox.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder from SecurityConfig
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // ----------------------------------------------------------
    // REGISTER: Create a new user account
    //
    // Steps:
    //   1. Check if email already exists (prevent duplicates)
    //   2. Hash the password with BCrypt
    //   3. Save new user to database
    //   4. Generate a JWT token (user is auto-logged in after register)
    //   5. Return token + user details to React
    // ----------------------------------------------------------
    public AuthResponse register(RegisterRequest request) {

        // Step 1: Check duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("An account with this email already exists. Please login.");
        }

        // Step 2: Build the User object
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // IMPORTANT: We NEVER store plain passwords!
        // BCrypt converts "mypassword123" → "$2a$10$randomhash..."
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole("ROLE_USER"); // All new users start as regular users

        // Step 3: Save to MySQL
        User savedUser = userRepository.save(user);

        // Step 4: Generate JWT token for auto-login after registration
        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole());

        // Step 5: Build and return response
        return AuthResponse.builder()
                .token(token)
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .userId(savedUser.getId())
                .role(savedUser.getRole())
                .message("Registration successful! Welcome to CakeBox 🎂")
                .build();
    }

    // ----------------------------------------------------------
    // LOGIN: Verify credentials and return JWT token
    //
    // Steps:
    //   1. Use Spring's AuthenticationManager to verify credentials
    //      (it automatically finds user by email + checks BCrypt password)
    //   2. If wrong credentials → AuthenticationManager throws exception
    //      → we don't need to handle it, Spring returns 401 automatically
    //   3. Load user from DB to get their name and role
    //   4. Generate JWT token
    //   5. Return token + user details to React
    // ----------------------------------------------------------
    public AuthResponse login(LoginRequest request) {

        // Step 1 & 2: Verify email + password
        // This one line does everything:
        //   - Finds user by email using CustomUserDetailsService
        //   - Compares BCrypt password
        //   - Throws BadCredentialsException if wrong (returns 401)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Step 3: Credentials verified! Load user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Step 4: Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        // Step 5: Return token + user info to React
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .userId(user.getId())
                .role(user.getRole())
                .message("Login successful! Welcome back, " + user.getName() + " 🎂")
                .build();
    }
}
