package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Door;

@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {
    
}