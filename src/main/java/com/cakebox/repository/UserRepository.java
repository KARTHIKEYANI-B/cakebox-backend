// ─────────────────────────────────────────────────────────────
// FILE 1: UserRepository.java
// ─────────────────────────────────────────────────────────────
package com.cakebox.repository;
 
import com.cakebox.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
 
@Repository
// JpaRepository<User, Long>
//   User → which model this manages
//   Long → data type of the primary key (id)
public interface UserRepository extends JpaRepository<User, Long> {
 
    // Spring generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);
 
    // Spring generates: SELECT COUNT(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);
}
 
 