package com.cakebox.model;

// =============================================================
// FILE: src/main/java/com/cakebox/model/Customization.java
//
// WHAT THIS FILE DOES:
// Stores full cake customization details.
// From your UI "Design Your Cake" section:
//   Step 1 → Flavor
//   Step 2 → Shape
//   Step 3 → Message
//   Step 4 → Upload Image (stored in Cloudinary)
// =============================================================

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customizations")
public class Customization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Step 1: Flavor
    // "Chocolate", "Vanilla", "Red Velvet", "Butterscotch", etc.
    private String flavor;

    // Step 2: Shape
    // "Round", "Square", "Heart", "Tier"
    private String shape;

    // Step 3: Message written on cake
    @Column(name = "cake_message")
    private String cakeMessage;

    // Step 4: Customer's uploaded reference image (stored in Cloudinary)
    @Column(name = "reference_image_url")
    private String referenceImageUrl;

    // Extra notes from customer for the baker
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    // ----------------------------------------------------------
    // RELATIONSHIP: One Customization → One OrderItem
    // ----------------------------------------------------------
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;
}