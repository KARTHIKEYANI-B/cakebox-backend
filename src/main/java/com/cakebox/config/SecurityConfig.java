package com.cakebox.config;

// =============================================================
// FILE: src/main/java/com/cakebox/config/SecurityConfig.java
//
// THIS IS THE MOST IMPORTANT SECURITY FILE.
// It answers the question: "Who can access what?"
//
// After adding this file:
//   ✅ The ugly default Spring login page DISAPPEARS
//   ✅ Public routes work without any token
//   ✅ Protected routes require a valid JWT token
//   ✅ Admin routes require ROLE_ADMIN
//   ✅ CORS is configured so React can call your API
// =============================================================

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration       // This is a configuration class
@EnableWebSecurity   // Enable Spring Security
@EnableMethodSecurity // Allows @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    // ----------------------------------------------------------
    // MAIN SECURITY RULES
    // This bean defines ALL security rules for your application.
    // ----------------------------------------------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── DISABLE CSRF ──────────────────────────────────
            // CSRF protection is for browser form-based apps.
            // We use JWT tokens instead, so CSRF is not needed.
            .csrf(AbstractHttpConfigurer::disable)

            // ── CORS CONFIGURATION ────────────────────────────
            // Allow your React frontend to call this API.
            // Without this, the browser blocks cross-origin requests.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── ROUTE ACCESS RULES ────────────────────────────
            // Order matters! More specific rules go first.
            .authorizeHttpRequests(auth -> auth

                // ✅ PUBLIC — No token required
                // Anyone can access these (login, register, browse cakes)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/*/reviews").permitAll()

                // 🔒 ADMIN ONLY — Must have ROLE_ADMIN
                // Shop owner managing products and orders
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                // 🔒 LOGGED IN USERS — Any authenticated user
                // Cart, orders, payment, profile, reviews
                .requestMatchers("/api/cart/**").authenticated()
                .requestMatchers("/api/orders/**").authenticated()
                .requestMatchers("/api/payment/**").authenticated()
                .requestMatchers("/api/customize/**").authenticated()
                .requestMatchers("/api/user/**").authenticated()

                // 🔒 Everything else also requires authentication
                .anyRequest().authenticated()
            )

            // ── SESSION POLICY ────────────────────────────────
            // STATELESS = no server-side sessions.
            // Every request must carry the JWT token.
            // This is the correct mode for REST APIs.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── AUTHENTICATION PROVIDER ───────────────────────
            // Tell Spring which UserDetailsService + PasswordEncoder to use
            .authenticationProvider(authenticationProvider())

            // ── JWT FILTER ────────────────────────────────────
            // Add our JWT filter BEFORE Spring's default filter.
            // This way, we authenticate via JWT before Spring checks.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ----------------------------------------------------------
    // CORS CONFIGURATION
    // CORS = Cross-Origin Resource Sharing
    //
    // Problem: React runs on localhost:5173
    //          Backend runs on localhost:8080
    // Browser considers these "different origins" and blocks requests.
    //
    // Solution: Tell the backend to ALLOW requests from React's origin.
    // ----------------------------------------------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from React dev server + production domain
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",   // Vite React dev server
                "http://localhost:3000",   // Create React App (just in case)
                "https://cakebox.vercel.app" // Your production domain (update this later)
        ));

        // Allow these HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow these headers (Authorization is needed for JWT)
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));

        // Allow cookies / Authorization header to be sent
        configuration.setAllowCredentials(true);

        // Apply this CORS config to ALL routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ----------------------------------------------------------
    // AUTHENTICATION PROVIDER
    // Connects Spring Security to YOUR database + password encoder
    // ----------------------------------------------------------
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // Load user from MySQL
        provider.setPasswordEncoder(passwordEncoder());     // Verify BCrypt password
        return provider;
    }

    // ----------------------------------------------------------
    // PASSWORD ENCODER
    // BCrypt is an industry-standard password hashing algorithm.
    //
    // How it works:
    //   Register: "mypassword" → BCrypt → "$2a$10$Xyz..." (stored in DB)
    //   Login: BCrypt checks if "mypassword" matches "$2a$10$Xyz..."
    //   Even if DB is hacked, passwords cannot be reversed.
    // ----------------------------------------------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ----------------------------------------------------------
    // AUTHENTICATION MANAGER
    // Used by AuthService to perform the actual login check.
    // ----------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}