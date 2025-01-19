package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.FurnitureDoor;

@Repository
public interface FurnitureDoorRepository extends JpaRepository<FurnitureDoor, Long> {
}
