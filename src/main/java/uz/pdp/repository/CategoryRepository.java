package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.entity.Category;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing door categories.
 * 
 * The warehouse where we store all our category secrets! 🗝️
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findAllByActiveTrue();

    Optional<Category> findByName(String securityDoors);
}
