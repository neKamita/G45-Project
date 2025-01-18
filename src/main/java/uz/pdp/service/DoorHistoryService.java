package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import uz.pdp.dto.UserDoorHistoryDto;
import uz.pdp.entity.Door;
import uz.pdp.entity.DoorHistory;
import uz.pdp.entity.User;
import uz.pdp.exception.ResourceNotFoundException;
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
     * Retrieves door access history for a specific door.
     *
     * @param doorId ID of the door
     * @return List of door history records
     * @throws ResourceNotFoundException if door not found
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public List<DoorHistory> getDoorHistory(Long doorId) {
        try {
            List<DoorHistory> history = doorHistoryRepository.findByDoorId(doorId);
            if (history.isEmpty()) {
                throw new ResourceNotFoundException("No history found for door ID: " + doorId);
            }
            return history;
        } catch (Exception e) {
            logger.error("Error retrieving door history - Door ID {}: {}", doorId, e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves access history for a specific user.
     *
     * @param userId ID of the user
     * @return List of door history records
     * @throws ResourceNotFoundException if user not found
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<DoorHistory> getUserHistory(Long userId) {
        try {
            List<DoorHistory> history = doorHistoryRepository.findByUserId(userId);
            if (history.isEmpty()) {
                throw new ResourceNotFoundException("No history found for user ID: " + userId);
            }
            return history;
        } catch (Exception e) {
            logger.error("Error retrieving user history - User ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves detailed history for a specific user and door combination.
     *
     * @param userId ID of the user
     * @param doorId ID of the door
     * @return UserDoorHistoryDto containing detailed history information
     * @throws ResourceNotFoundException if no history found
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    public UserDoorHistoryDto getUserDoorHistory(Long userId, Long doorId) {
        try {
            List<DoorHistory> histories = doorHistoryRepository.findByUserIdAndDoorId(userId, doorId);
            if (histories.isEmpty()) {
                throw new ResourceNotFoundException(
                    String.format("No history found for user ID: %d and door ID: %d", userId, doorId));
            }

            return mapToUserDoorHistoryDto(histories);
        } catch (Exception e) {
            logger.error("Error retrieving user-door history - User ID {}, Door ID {}: {}", 
                userId, doorId, e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves access history for a specific user with grouping.
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

    /**
     * Retrieves the most recent door access events.
     *
     * @param limit Maximum number of records to retrieve
     * @return List of recent door history records
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<DoorHistory> getRecentHistory(int limit) {
        try {
            return doorHistoryRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "accessedAt"))
            ).getContent();
        } catch (Exception e) {
            logger.error("Error retrieving recent history - Limit {}: {}", limit, e.getMessage());
            throw e;
        }
    }

    /**
     * Maps a list of DoorHistory objects to a UserDoorHistoryDto.
     *
     * @param histories List of DoorHistory objects
     * @return UserDoorHistoryDto containing detailed history information
     */
    private UserDoorHistoryDto mapToUserDoorHistoryDto(List<DoorHistory> histories) {
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