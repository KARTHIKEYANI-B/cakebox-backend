
// ─────────────────────────────────────────────────────────────
// FILE 1: RegisterRequest.java
// What data React sends when a NEW USER signs up
//
// React POST body example:
// {
//   "name": "Ravi Kumar",
//   "email": "ravi@gmail.com",
//   "password": "mypassword123",
//   "phoneNumber": "9876543210"
// }
// ─────────────────────────────────────────────────────────────
package com.cakebox.dto;
 
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
 
@Data
public class RegisterRequest {
 
    @NotBlank(message = "Name is required")
    private String name;
 
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;
 
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
 
    private String phoneNumber; // Optional
}
 
