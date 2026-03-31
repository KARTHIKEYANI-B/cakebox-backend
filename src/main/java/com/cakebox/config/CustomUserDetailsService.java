package com.cakebox.config;

// =============================================================
// FILE: src/main/java/com/cakebox/config/CustomUserDetailsService.java
//
// WHY DO WE NEED THIS?
// Spring Security needs to load user details to verify login.
// But Spring doesn't know about YOUR database or User model.
// This class acts as a BRIDGE:
//   Spring Security asks → "Give me user with email X"
//   We query MySQL → find the User → return it to Spring
//
// Spring Security then uses the returned user to:
//   - Compare the password during login
//   - Set the user's role (ROLE_USER / ROLE_ADMIN)
// =============================================================

import com.cakebox.model.User;
import com.cakebox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor  // Lombok: auto-generates constructor with all final fields
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // ----------------------------------------------------------
    // Spring Security calls this with the email (username)
    // We load the user from MySQL and return a UserDetails object
    // ----------------------------------------------------------
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Find user in database by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with email: " + email
                ));

        // Build and return Spring Security's UserDetails object
        // This is what Spring uses internally for auth checks
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),       // username (we use email as username)
                user.getPassword(),    // encrypted BCrypt password
                Collections.singletonList(
                        // Converts "ROLE_USER" or "ROLE_ADMIN" into Spring's format
                        new SimpleGrantedAuthority(user.getRole())
                )
        );
    }
}