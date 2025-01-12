package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Door;

import java.util.List;

@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {

    List<Door> findBySellerId(Long userId);
}