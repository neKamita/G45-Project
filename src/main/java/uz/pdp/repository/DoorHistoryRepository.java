package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.entity.DoorHistory;
import java.util.List;

public interface DoorHistoryRepository extends JpaRepository<DoorHistory, Long> {
    List<DoorHistory> findByUserId(Long userId);
}