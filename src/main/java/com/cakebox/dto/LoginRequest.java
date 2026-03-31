
// ─────────────────────────────────────────────────────────────
// FILE 2: LoginRequest.java
// What data React sends when user LOGS IN
//
// React POST body example:
// {
//   "email": "ravi@gmail.com",
//   "password": "mypassword123"
// }
// ─────────────────────────────────────────────────────────────
package com.cakebox.dto;
 
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
 
@Data
public class LoginRequest {
 
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email")
    private String email;
 
    @NotBlank(message = "Password is required")
    private String password;
}
 