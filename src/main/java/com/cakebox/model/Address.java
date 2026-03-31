package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/Address.java
//
// WHAT THIS FILE DOES:
// Stores delivery addresses for users.
// From your UI: checkout page has address form.
// One user can save multiple addresses.
// =============================================================

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pincode;

    // ----------------------------------------------------------
    // DEFAULT ADDRESS FLAG
    // If true, this address is auto-selected at checkout
    // ----------------------------------------------------------
    @Column(name = "is_default")
    private Boolean isDefault = false;

    // ----------------------------------------------------------
    // RELATIONSHIP: Many Addresses → One User
    // ----------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}