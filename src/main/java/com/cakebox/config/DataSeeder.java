package com.cakebox.config;

// =============================================================
// FILE: src/main/java/com/cakebox/config/DataSeeder.java
//
// WHAT THIS DOES:
// Automatically inserts sample data into your MySQL database
// the FIRST TIME the app starts (only if tables are empty).
//
// After running, you'll have:
//   - 4 categories (Cakes, Brownies, Sweets, Chocolates)
//   - 8 sample cake products with real Unsplash image URLs
//   - 1 admin user (admin@cakebox.com / admin123)
//
// This is only for development/testing.
// On Railway (production), this still runs but only if DB is empty.
//
// @Component + CommandLineRunner → runs automatically on startup
// =============================================================

import com.cakebox.model.Category;
import com.cakebox.model.Product;
import com.cakebox.model.User;
import com.cakebox.repository.CategoryRepository;
import com.cakebox.repository.ProductRepository;
import com.cakebox.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if database is empty
        if (categoryRepository.count() == 0) {
            seedCategories();
        }
        if (productRepository.count() == 0) {
            seedProducts();
        }
        if (userRepository.count() == 0) {
            seedAdminUser();
        }
    }

    // ----------------------------------------------------------
    // SEED CATEGORIES
    // ----------------------------------------------------------
    private void seedCategories() {
        log.info("🌱 Seeding categories...");

        Category cakes = createCategory("Cakes", "Fresh baked cakes for every occasion",
                "https://images.unsplash.com/photo-1621303837174-89787a7d4729?w=400", "ALL", 1);
        Category brownies = createCategory("Brownies", "Rich fudgy chocolate brownies",
                "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=400", "ALL", 2);
        Category sweets = createCategory("Sweets", "Traditional Indian mithai and sweets",
                "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?w=400", "FESTIVAL", 3);
        Category chocolates = createCategory("Chocolates", "Premium Belgian chocolates",
                "https://images.unsplash.com/photo-1511381939415-e44015466834?w=400", "ALL", 4);

        categoryRepository.saveAll(Arrays.asList(cakes, brownies, sweets, chocolates));
        log.info("✅ Categories seeded: Cakes, Brownies, Sweets, Chocolates");
    }

    private Category createCategory(String name, String desc, String imageUrl,
                                     String occasion, int order) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(desc);
        c.setImageUrl(imageUrl);
        c.setOccasionTag(occasion);
        c.setDisplayOrder(order);
        c.setIsActive(true);
        return c;
    }

    // ----------------------------------------------------------
    // SEED PRODUCTS
    // ----------------------------------------------------------
    private void seedProducts() {
        log.info("🌱 Seeding products...");

        // Get categories
        Category cakes = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Cakes")).findFirst().orElse(null);
        Category brownies = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Brownies")).findFirst().orElse(null);
        Category chocolates = categoryRepository.findAll().stream()
                .filter(c -> c.getName().equals("Chocolates")).findFirst().orElse(null);

        if (cakes == null) return;

        // ── Product 1: Red Velvet Bliss ─────────────────
        Product p1 = new Product();
        p1.setName("Red Velvet Bliss");
        p1.setDescription("A rich, moist red velvet cake with smooth cream cheese frosting. " +
                "Perfectly layered with a velvety texture that melts in your mouth.");
        p1.setPrice(new BigDecimal("1199.00"));
        p1.setDiscountPrice(new BigDecimal("999.00"));
        p1.setMainImageUrl("https://images.unsplash.com/photo-1586788680434-30d324b2d46f?w=800");
        p1.setAvailableFlavors(Arrays.asList("Red Velvet", "Chocolate", "Vanilla"));
        p1.setAvailableSizes(Arrays.asList("0.5", "1", "2"));
        p1.setEgglessAvailable(true);
        p1.setOccasionTag("ANNIVERSARY");
        p1.setIsTrending(true);
        p1.setStockQuantity(50);
        p1.setAverageRating(new BigDecimal("4.8"));
        p1.setTotalRatings(124);
        p1.setCategory(cakes);
        p1.setIsActive(true);

        // ── Product 2: Chocolate Truffle ────────────────
        Product p2 = new Product();
        p2.setName("Chocolate Truffle Supreme");
        p2.setDescription("Indulgent dark chocolate truffle cake with ganache frosting. " +
                "A chocoholic's dream come true.");
        p2.setPrice(new BigDecimal("1099.00"));
        p2.setDiscountPrice(new BigDecimal("899.00"));
        p2.setMainImageUrl("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800");
        p2.setAvailableFlavors(Arrays.asList("Chocolate", "Dark Chocolate"));
        p2.setAvailableSizes(Arrays.asList("0.5", "1", "2"));
        p2.setEgglessAvailable(true);
        p2.setOccasionTag("BIRTHDAY");
        p2.setIsTrending(true);
        p2.setStockQuantity(60);
        p2.setAverageRating(new BigDecimal("4.9"));
        p2.setTotalRatings(238);
        p2.setCategory(cakes);
        p2.setIsActive(true);

        // ── Product 3: Butterscotch Dream ───────────────
        Product p3 = new Product();
        p3.setName("Butterscotch Dream");
        p3.setDescription("Light and fluffy butterscotch cake with caramel drizzle. " +
                "A classic favourite that never disappoints.");
        p3.setPrice(new BigDecimal("899.00"));
        p3.setDiscountPrice(new BigDecimal("749.00"));
        p3.setMainImageUrl("https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?w=800");
        p3.setAvailableFlavors(Arrays.asList("Butterscotch", "Vanilla", "Caramel"));
        p3.setAvailableSizes(Arrays.asList("0.5", "1", "2"));
        p3.setEgglessAvailable(true);
        p3.setOccasionTag("BIRTHDAY");
        p3.setIsTrending(false);
        p3.setStockQuantity(40);
        p3.setAverageRating(new BigDecimal("4.6"));
        p3.setTotalRatings(89);
        p3.setCategory(cakes);
        p3.setIsActive(true);

        // ── Product 4: Fresh Fruit Cake ─────────────────
        Product p4 = new Product();
        p4.setName("Fresh Fruit Gâteau");
        p4.setDescription("Light sponge cake topped with seasonal fresh fruits and whipped cream. " +
                "Perfect for summer celebrations.");
        p4.setPrice(new BigDecimal("1299.00"));
        p4.setDiscountPrice(new BigDecimal("1099.00"));
        p4.setMainImageUrl("https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=800");
        p4.setAvailableFlavors(Arrays.asList("Vanilla", "White Chocolate"));
        p4.setAvailableSizes(Arrays.asList("0.5", "1", "2"));
        p4.setEgglessAvailable(false);
        p4.setOccasionTag("WEDDING");
        p4.setIsTrending(true);
        p4.setStockQuantity(30);
        p4.setAverageRating(new BigDecimal("4.7"));
        p4.setTotalRatings(67);
        p4.setCategory(cakes);
        p4.setIsActive(true);

        // ── Product 5: Birthday Blast ───────────────────
        Product p5 = new Product();
        p5.setName("Birthday Blast");
        p5.setDescription("A fun, colourful cake loaded with rainbow sprinkles and buttercream. " +
                "Makes every birthday extra special!");
        p5.setPrice(new BigDecimal("799.00"));
        p5.setDiscountPrice(new BigDecimal("649.00"));
        p5.setMainImageUrl("https://images.unsplash.com/photo-1558301211-0d8c8ddee6ec?w=800");
        p5.setAvailableFlavors(Arrays.asList("Vanilla", "Chocolate", "Strawberry"));
        p5.setAvailableSizes(Arrays.asList("0.5", "1", "2"));
        p5.setEgglessAvailable(true);
        p5.setOccasionTag("BIRTHDAY");
        p5.setIsTrending(true);
        p5.setStockQuantity(80);
        p5.setAverageRating(new BigDecimal("4.5"));
        p5.setTotalRatings(156);
        p5.setCategory(cakes);
        p5.setIsActive(true);

        // ── Product 6: Classic Brownie Box ──────────────
        if (brownies != null) {
            Product p6 = new Product();
            p6.setName("Classic Brownie Box");
            p6.setDescription("A box of 6 fudgy chocolate brownies with walnuts. " +
                    "Freshly baked and packed with chocolatey goodness.");
            p6.setPrice(new BigDecimal("499.00"));
            p6.setDiscountPrice(new BigDecimal("399.00"));
            p6.setMainImageUrl("https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=800");
            p6.setAvailableFlavors(Arrays.asList("Chocolate", "Dark Chocolate"));
            p6.setAvailableSizes(Arrays.asList("6 pcs", "12 pcs", "24 pcs"));
            p6.setEgglessAvailable(true);
            p6.setOccasionTag("ALL");
            p6.setIsTrending(true);
            p6.setStockQuantity(100);
            p6.setAverageRating(new BigDecimal("4.7"));
            p6.setTotalRatings(203);
            p6.setCategory(brownies);
            p6.setIsActive(true);
            productRepository.save(p6);
        }

        // ── Product 7: Festival Special ─────────────────
        Product p7 = new Product();
        p7.setName("Diwali Special Dry Fruit Cake");
        p7.setDescription("A rich dry fruit cake loaded with cashews, almonds, and raisins. " +
                "Perfect Diwali gift for your loved ones.");
        p7.setPrice(new BigDecimal("1499.00"));
        p7.setDiscountPrice(new BigDecimal("1199.00"));
        p7.setMainImageUrl("https://images.unsplash.com/photo-1480253054901-8f7ae57be2e3?w=800");
        p7.setAvailableFlavors(Arrays.asList("Dry Fruit", "Chocolate Dry Fruit"));
        p7.setAvailableSizes(Arrays.asList("0.5", "1", "2"));
        p7.setEgglessAvailable(true);
        p7.setOccasionTag("FESTIVAL");
        p7.setIsTrending(false);
        p7.setStockQuantity(40);
        p7.setAverageRating(new BigDecimal("4.8"));
        p7.setTotalRatings(92);
        p7.setCategory(cakes);
        p7.setIsActive(true);

        // ── Product 8: Wedding Tier Cake ─────────────────
        Product p8 = new Product();
        p8.setName("Royal Wedding Tier");
        p8.setDescription("A stunning 3-tier wedding cake with fondant roses and gold accents. " +
                "Custom designed to make your wedding unforgettable.");
        p8.setPrice(new BigDecimal("5999.00"));
        p8.setDiscountPrice(new BigDecimal("4999.00"));
        p8.setMainImageUrl("https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=800");
        p8.setAvailableFlavors(Arrays.asList("Vanilla", "Chocolate", "Red Velvet"));
        p8.setAvailableSizes(Arrays.asList("2", "4", "6"));
        p8.setEgglessAvailable(true);
        p8.setOccasionTag("WEDDING");
        p8.setIsTrending(true);
        p8.setStockQuantity(10);
        p8.setAverageRating(new BigDecimal("5.0"));
        p8.setTotalRatings(34);
        p8.setCategory(cakes);
        p8.setIsActive(true);

        productRepository.saveAll(Arrays.asList(p1, p2, p3, p4, p5, p7, p8));
        log.info("✅ Products seeded: 8 sample products added");
    }

    // ----------------------------------------------------------
    // SEED ADMIN USER
    // Login: admin@cakebox.com / admin123
    // CHANGE THIS PASSWORD after first login!
    // ----------------------------------------------------------
    private void seedAdminUser() {
        log.info("🌱 Seeding admin user...");

        User admin = new User();
        admin.setName("CakeBox Admin");
        admin.setEmail("admin@cakebox.com");
        admin.setPassword(passwordEncoder.encode("admin123")); // BCrypt hashed
        admin.setRole("ROLE_ADMIN");
        admin.setPhoneNumber("9876543210");

        userRepository.save(admin);
        log.info("✅ Admin user created: admin@cakebox.com / admin123");
        log.warn("⚠️  CHANGE THE ADMIN PASSWORD AFTER FIRST LOGIN!");
    }
}