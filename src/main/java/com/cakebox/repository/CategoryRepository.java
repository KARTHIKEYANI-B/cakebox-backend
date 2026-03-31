 
 
// ─────────────────────────────────────────────────────────────
// FILE 2: CategoryRepository.java
// ─────────────────────────────────────────────────────────────
package com.cakebox.repository;
 
import com.cakebox.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
 
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
 
    // Get all active (visible) categories, sorted by display order
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
 
    // Get categories by occasion tag
    // e.g. findByOccasionTag("BIRTHDAY") → Birthday cake categories
    List<Category> findByOccasionTag(String occasionTag);
}
 