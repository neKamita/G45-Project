package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Location;
import uz.pdp.entity.Storage;

/**
 * Repository for managing storage locations.
 * Where all our storage secrets are kept safe! 🗄️
 */
@Repository
public interface StorageRepository extends JpaRepository<Storage, Long> {
    // Add custom queries here if needed in the future
}
