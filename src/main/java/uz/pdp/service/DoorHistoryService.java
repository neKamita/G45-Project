package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uz.pdp.dto.UserDoorHistoryDto;
import uz.pdp.entity.Door;
import uz.pdp.entity.DoorHistory;
import uz.pdp.entity.User;
import uz.pdp.repository.DoorHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing door access history.
 * Tracks and records all door access events including successful and failed attempts.
 * Provides functionality for querying and analyzing door usage patterns.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class DoorHistoryService {
    private static final Logger logger = LoggerFactory.getLogger(DoorHistoryService.class);

    @Autowired
    private DoorHistoryRepository doorHistoryRepository;

    @Autowired
    private UserService userService;

    /**
     * Records a new door access event.
     * Captures user, door, and access details.
     *
     * @param door Door being accessed
     */
    public void saveDoorHistory(Door door) {
        User currentUser = userService.getCurrentUser();
        DoorHistory history = new DoorHistory();
        history.setDoor(door);
        history.setUser(currentUser);
        history.setAccessedAt(LocalDateTime.now());
        doorHistoryRepository.save(history);
    }

    /**
     * Retrieves access history for a specific user.
     *
     * @param userId ID of the user
     * @return DTO containing user's door history
     */
    public UserDoorHistoryDto getUserDoorHistoryGrouped(Long userId) {
        List<DoorHistory> histories = doorHistoryRepository.findByUserId(userId);
        if (histories.isEmpty()) {
            return null;
        }

        UserDoorHistoryDto dto = new UserDoorHistoryDto();
        
        // Set only necessary user info once
        User user = histories.get(0).getUser();
        UserDoorHistoryDto.UserBasicInfo userInfo = new UserDoorHistoryDto.UserBasicInfo();
        userInfo.setId(user.getId());
        userInfo.setName(user.getName());
        userInfo.setEmail(user.getEmail());
        dto.setUser(userInfo);

        // Map only necessary door info
        List<UserDoorHistoryDto.DoorHistoryEntry> historyEntries = histories.stream()
            .map(history -> {
                UserDoorHistoryDto.DoorHistoryEntry entry = new UserDoorHistoryDto.DoorHistoryEntry();
                entry.setId(history.getId());
                entry.setAccessedAt(history.getAccessedAt());
                
                Door door = history.getDoor();
                UserDoorHistoryDto.DoorBasicInfo doorInfo = new UserDoorHistoryDto.DoorBasicInfo();
                doorInfo.setId(door.getId());
                doorInfo.setName(door.getName());
                doorInfo.setPrice(door.getPrice());
                doorInfo.setFinalPrice(door.getFinalPrice());
                doorInfo.setSize(door.getSize().toString());
                doorInfo.setColor(door.getColor().toString());
                doorInfo.setMaterial(door.getMaterial());
                doorInfo.setSellerId(door.getSeller().getId());
                
                entry.setDoor(doorInfo);
                return entry;
            })
            .collect(Collectors.toList());

        dto.setHistory(historyEntries);
        return dto;
    }
}