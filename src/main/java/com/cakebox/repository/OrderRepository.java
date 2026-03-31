
// ─────────────────────────────────────────────────────────────
// FILE 5: OrderRepository.java
// ─────────────────────────────────────────────────────────────
package com.cakebox.repository;
 
import com.cakebox.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
 
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
 
    // All orders for a user (newest first) — for order history page
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
 
    // Find order by Razorpay order ID (used during payment verification)
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
 
    // Admin: all orders newest first
    List<Order> findAllByOrderByCreatedAtDesc();
}
 