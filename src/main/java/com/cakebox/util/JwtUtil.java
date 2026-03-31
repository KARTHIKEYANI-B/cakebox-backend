package com.cakebox.util;

// =============================================================
// FILE: src/main/java/com/cakebox/util/JwtUtil.java
//
// WHAT IS A JWT TOKEN?
// JWT = JSON Web Token. It looks like this:
//   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGdtYWlsLmNvbSJ9.xyzABC
//
// It has 3 parts separated by dots:
//   Part 1 (Header)  → algorithm used
//   Part 2 (Payload) → user data (email, role, expiry)
//   Part 3 (Signature) → proves it wasn't tampered with
//
// HOW IT WORKS:
//   1. User logs in → we create a JWT with their email + role
//   2. We send JWT to React → React saves it in localStorage
//   3. React sends JWT in every request header:
//      Authorization: Bearer eyJhbGci...
//   4. Our filter reads the JWT, verifies it, extracts email
//   5. We know who the user is without checking the DB again!
// =============================================================

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component  // Makes this available everywhere via @Autowired
public class JwtUtil {

    // Reads jwt.secret from application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Reads jwt.expiration from application.properties (86400000 = 24 hours)
    @Value("${jwt.expiration}")
    private Long expiration;

    // ----------------------------------------------------------
    // STEP 1: CREATE THE SIGNING KEY
    // Converts our secret string into a cryptographic key
    // ----------------------------------------------------------
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ----------------------------------------------------------
    // STEP 2: GENERATE TOKEN
    // Called in AuthService after user logs in successfully.
    //
    // We store in the token:
    //   "sub" (subject) → user's email
    //   "role"          → ROLE_USER or ROLE_ADMIN
    //   "iat"           → issued at (current time)
    //   "exp"           → expiry (current time + 24 hours)
    // ----------------------------------------------------------
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);  // Store role so we don't need DB lookup on every request

        return Jwts.builder()
                .claims(claims)
                .subject(email)               // Who this token belongs to
                .issuedAt(new Date())          // When created
                .expiration(new Date(System.currentTimeMillis() + expiration)) // When expires
                .signWith(getSigningKey())     // Sign with our secret key
                .compact();                   // Build the token string
    }

    // ----------------------------------------------------------
    // STEP 3: READ DATA FROM TOKEN
    // These methods extract information stored inside the token.
    // ----------------------------------------------------------

    // Extract all claims (the payload data) from token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // Verify signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Generic method to extract any specific claim
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract email (subject) from token
    // Used in JwtAuthFilter to find which user made the request
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract role from token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Extract expiry date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ----------------------------------------------------------
    // STEP 4: VALIDATE TOKEN
    // Checks:
    //   1. Email in token matches the user we loaded from DB
    //   2. Token has not expired yet
    // ----------------------------------------------------------
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
