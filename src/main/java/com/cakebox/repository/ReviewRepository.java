
// ─────────────────────────────────────────────────────────────
// FILE 6: ReviewRepository.java
// ─────────────────────────────────────────────────────────────
package com.cakebox.repository;
 
import com.cakebox.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
 
    // All reviews for a product, newest first
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
 
    // Check if user already reviewed this product
    Optional<Review> findByUserIdAndProductId(Long userId, Long productId);
 
    // Calculate average rating for a product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
 
    // Count total reviews for a product
    Long countByProductId(Long productId);
}
 
