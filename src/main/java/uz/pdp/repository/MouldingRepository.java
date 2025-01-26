package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Moulding;

@Repository
public interface MouldingRepository extends JpaRepository<Moulding, Long> {
    // Provides built-in methods for CRUD operations (e.g., save, findById, findAll, deleteById)
}