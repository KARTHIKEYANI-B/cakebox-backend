 
// ─────────────────────────────────────────────────────────────
// FILE 3: ProductRepository.java
// ─────────────────────────────────────────────────────────────
package com.cakebox.repository;
 
import com.cakebox.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
 
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
 
    // All active products (not soft-deleted)
    List<Product> findByIsActiveTrue();
 
    // Products by category
    List<Product> findByCategoryIdAndIsActiveTrue(Long categoryId);
 
    // Products by occasion (Birthday, Anniversary, etc.)
    List<Product> findByOccasionTagAndIsActiveTrue(String occasionTag);
 
    // Trending / Popular products (for "Popular Near You" section)
    List<Product> findByIsTrendingTrueAndIsActiveTrue();
 
    // Search products by name (LIKE %keyword%)
    // @Query lets us write custom JPQL (similar to SQL)
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND p.isActive = true")
    List<Product> searchByName(@Param("keyword") String keyword);
}
 