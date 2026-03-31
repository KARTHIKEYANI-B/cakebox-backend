 
// ─────────────────────────────────────────────────────────────
// FILE 4: CartRepository.java
// ─────────────────────────────────────────────────────────────
package com.cakebox.repository;
 
import com.cakebox.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
 
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
 
    // Get the cart for a specific user
    Optional<Cart> findByUserId(Long userId);
}