package com.cakebox.config;

// =============================================================
// FILE: src/main/java/com/cakebox/config/JwtAuthenticationFilter.java
//
// WHAT IS A FILTER?
// Every HTTP request passes through this filter BEFORE
// reaching your controller.
//
// WHAT THIS FILTER DOES (runs on every request):
//
// Step 1: Read the "Authorization" header
//         Example header: "Bearer eyJhbGciOiJIUzI1NiJ9..."
//
// Step 2: Extract the token (remove "Bearer " prefix)
//
// Step 3: Extract the email from the token using JwtUtil
//
// Step 4: Load user from database using that email
//
// Step 5: Validate the token (correct + not expired)
//
// Step 6: If valid → tell Spring Security "this user is logged in"
//
// If no token → request passes through but user is not authenticated
// (SecurityConfig will then reject protected routes)
// =============================================================

import com.cakebox.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter → guarantees this runs exactly ONCE per request

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain      // the rest of the filter chain
    ) throws ServletException, IOException {

        // ──────────────────────────────────────────────────────
        // STEP 1: Read the Authorization header
        // Example: "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIi..."
        // ──────────────────────────────────────────────────────
        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header, or it doesn't start with "Bearer "
        // → this is a public request (login, register, view products)
        // → just pass it through without authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ──────────────────────────────────────────────────────
        // STEP 2: Extract the token (remove "Bearer " prefix)
        // "Bearer eyJhbGci..." → "eyJhbGci..."
        // ──────────────────────────────────────────────────────
        final String jwt = authHeader.substring(7);

        // ──────────────────────────────────────────────────────
        // STEP 3: Extract email from token
        // ──────────────────────────────────────────────────────
        final String email;
        try {
            email = jwtUtil.extractEmail(jwt);
        } catch (Exception e) {
            // Token is malformed or tampered → reject
            filterChain.doFilter(request, response);
            return;
        }

        // ──────────────────────────────────────────────────────
        // STEP 4: If email found AND user not already authenticated
        // (SecurityContextHolder.getContext().getAuthentication() == null
        //  means user is not yet authenticated in this request)
        // ──────────────────────────────────────────────────────
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user from database
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // ──────────────────────────────────────────────────
            // STEP 5: Validate token
            // ──────────────────────────────────────────────────
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // ──────────────────────────────────────────────
                // STEP 6: Create authentication object and set it
                // This tells Spring Security:
                // "This request is made by [email], with roles [ROLE_USER]"
                // ──────────────────────────────────────────────
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials (null = already verified)
                                userDetails.getAuthorities()   // roles
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Set authentication in Spring's security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Pass request to the next filter (or controller)
        filterChain.doFilter(request, response);
    }
}