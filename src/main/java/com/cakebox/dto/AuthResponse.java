
 
// ─────────────────────────────────────────────────────────────
// FILE 3: AuthResponse.java
// What YOUR API sends BACK to React after login/register
//
// API response example:
// {
//   "token": "eyJhbGciOiJIUzI1NiJ9...",
//   "email": "ravi@gmail.com",
//   "name": "Ravi Kumar",
//   "role": "ROLE_USER",
//   "message": "Login successful"
// }
//
// React saves the "token" in localStorage.
// React shows user's name in the Navbar.
// ─────────────────────────────────────────────────────────────
package com.cakebox.dto;
 
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@Builder           // Lombok: lets us build object like AuthResponse.builder().token("xyz").build()
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
 
    private String token;    // The JWT token
    private String email;
    private String name;
    private Long userId;
    private String role;     // "ROLE_USER" or "ROLE_ADMIN"
    private String message;  // "Login successful" or "Registration successful"
}
 
