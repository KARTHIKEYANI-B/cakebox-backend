package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/User.java
//
// WHAT THIS FILE DOES:
// Defines the "users" table in MySQL.
// Every field below = one column in the database.
//
// @Entity   → tells Spring "this class is a database table"
// @Table    → sets the exact table name in MySQL
// @Id      → marks which field is the primary key
// @Column  → customizes column properties (nullable, unique, etc.)
// @Lombok  → @Data auto-generates getters/setters/toString
// =============================================================

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data                   // Lombok: generates getters, setters, toString, equals, hashCode
@NoArgsConstructor      // Lombok: generates empty constructor User() {}
@AllArgsConstructor     // Lombok: generates constructor with all fields
@Entity                 // JPA: this class maps to a database table
@Table(name = "users")  // MySQL table will be named "users"
public class User {

    // ----------------------------------------------------------
    // PRIMARY KEY
    // @GeneratedValue → MySQL auto-increments: 1, 2, 3, 4...
    // ----------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ----------------------------------------------------------
    // NAME
    // nullable = false → this column cannot be empty in DB
    // ----------------------------------------------------------
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;

    // ----------------------------------------------------------
    // EMAIL
    // unique = true → no two users can have same email
    // @Email → validates format like "user@example.com"
    // ----------------------------------------------------------
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Column(nullable = false, unique = true)
    private String email;

    // ----------------------------------------------------------
    // PASSWORD
    // We NEVER store plain passwords!
    // Before saving, we encrypt with BCrypt (done in AuthService)
    // Example: "mypassword123" becomes "$2a$10$abc..."
    // ----------------------------------------------------------
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(nullable = false)
    private String password;

    // ----------------------------------------------------------
    // PHONE NUMBER (optional)
    // ----------------------------------------------------------
    @Column(name = "phone_number")
    private String phoneNumber;

    // ----------------------------------------------------------
    // ROLE
    // Controls what a user can access:
    //   ROLE_USER  → regular customer
    //   ROLE_ADMIN → shop owner (can add/delete products, manage orders)
    //
    // Default is ROLE_USER for all new registrations.
    // To make an admin, manually update the DB:
    //   UPDATE users SET role='ROLE_ADMIN' WHERE email='admin@cakebox.com';
    // ----------------------------------------------------------
    @Column(nullable = false)
    private String role = "ROLE_USER";

    // ----------------------------------------------------------
    // TIMESTAMPS
    // @CreationTimestamp → auto-set when record is CREATED
    // @UpdateTimestamp   → auto-set whenever record is UPDATED
    // updatable = false  → createdAt never changes after creation
    // ----------------------------------------------------------
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------
    // RELATIONSHIPS
    //
    // @OneToMany → one User has many Addresses
    // mappedBy = "user" → the Address class has a field called "user"
    // cascade = ALL → if user deleted, addresses are deleted too
    // fetch = LAZY → don't load addresses unless specifically asked
    //               (LAZY is better for performance)
    // ----------------------------------------------------------
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();
}