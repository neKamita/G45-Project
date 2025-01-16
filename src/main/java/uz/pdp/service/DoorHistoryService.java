package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.entity.Door;
import uz.pdp.entity.DoorHistory;
import uz.pdp.entity.User;
import uz.pdp.repository.DoorHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoorHistoryService {

    @Autowired
    private DoorHistoryRepository doorHistoryRepository;

    @Autowired
    private UserService userService;

    public void saveDoorHistory(Door door) {
        User currentUser = userService.getCurrentUser();
        DoorHistory doorHistory = new DoorHistory();
        doorHistory.setUser(currentUser);
        doorHistory.setDoor(door);
        doorHistory.setAccessedAt(LocalDateTime.now());
        doorHistoryRepository.save(doorHistory);
    }

    public List<DoorHistory> getUserDoorHistory(Long userId) {
        return doorHistoryRepository.findByUserId(userId);
    }
}