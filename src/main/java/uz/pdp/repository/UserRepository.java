package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing users - where all the magic user data lives! 🧙‍♂️
 * Handle with care, or users might disappear into the void!
 */
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.name = :name AND u.active = true")
    Optional<User> findByName(@Param("name") String name);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.name = :name")
    boolean existsByName(@Param("name") String name);
    
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.active = true ORDER BY u.id")
    List<User> findAllByRole(@Param("role") Role role);
    
    @Query("SELECT u FROM User u WHERE u.sellerRequestPending = true AND u.active = true ORDER BY u.id")
    List<User> findAllPendingSellers();
}