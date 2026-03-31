package com.cakebox.exception;

// =============================================================
// FILE: src/main/java/com/cakebox/exception/GlobalExceptionHandler.java
//
// WHY DO WE NEED THIS?
// Without this, errors return ugly HTML pages or stack traces.
// With this, all errors return clean JSON like:
// {
//   "error": "User not found",
//   "status": 404
// }
//
// React can then read the "error" field and show a nice message.
// =============================================================

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice  // Applies to ALL controllers in the app
public class GlobalExceptionHandler {

    // ----------------------------------------------------------
    // Handle validation errors (@NotBlank, @Email, @Size failures)
    // Returns all field errors at once so React can show them all
    //
    // Example response:
    // {
    //   "email": "Please enter a valid email",
    //   "password": "Password must be at least 6 characters"
    // }
    // ----------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // ----------------------------------------------------------
    // Handle wrong email/password during login
    // ----------------------------------------------------------
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            BadCredentialsException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid email or password");
        error.put("status", "401");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ----------------------------------------------------------
    // Handle "not found" errors (product not found, user not found, etc.)
    // ----------------------------------------------------------
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(
            RuntimeException ex) {

        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ----------------------------------------------------------
    // Handle any other unexpected error
    // ----------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Something went wrong. Please try again.");
        error.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}