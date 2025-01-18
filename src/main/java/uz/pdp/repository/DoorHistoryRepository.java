package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.DoorHistory;
import java.util.List;

@Repository
public interface DoorHistoryRepository extends JpaRepository<DoorHistory, Long> {
    
    List<DoorHistory> findByUserId(Long userId);
    
    @Modifying
    @Query("DELETE FROM DoorHistory dh WHERE dh.door.id = :doorId")
    void deleteByDoorId(Long doorId);

    List<DoorHistory> findByDoorId(Long doorId);

    List<DoorHistory> findByUserIdAndDoorId(Long userId, Long doorId);
}