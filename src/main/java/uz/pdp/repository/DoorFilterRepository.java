package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Door;

/**
 * Repository for filtering doors with complex criteria.
 * 
 * This repository is like a master key - it can open any door in our collection! 🗝️✨
 */
@Repository
public interface DoorFilterRepository extends JpaRepository<Door, Long>, JpaSpecificationExecutor<Door> {
    // JpaSpecificationExecutor provides dynamic filtering capabilities
}
