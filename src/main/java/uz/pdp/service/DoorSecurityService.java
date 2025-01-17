package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import uz.pdp.entity.Door;
import uz.pdp.entity.User;
import uz.pdp.repository.DoorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

/**
 * Service class for managing door security operations.
 * Handles access control, permissions, and security validations for door operations.
 * Implements role-based access control for door management.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class DoorSecurityService {
    private static final Logger logger = LoggerFactory.getLogger(DoorSecurityService.class);

    @Autowired
    private DoorRepository doorRepository;
    
    @Autowired
    private UserService userService;

    /**
     * Checks if the current user is the seller of a specific door.
     *
     * @param doorId ID of the door to check
     * @return true if current user is the seller, false otherwise
     * @throws EntityNotFoundException if door not found
     */
    public boolean isSeller(Long doorId) {
        try {
            logger.info("Checking seller status for door ID: {}", doorId);
            Door door = doorRepository.findById(doorId)
                .orElseThrow(() -> new EntityNotFoundException("Door not found"));
            
            User currentUser = userService.getCurrentUser();
            boolean isSeller = door.getSeller().getId().equals(currentUser.getId());
            
            logger.debug("User {} is{} the seller of door {}", 
                currentUser.getId(), isSeller ? "" : " not", doorId);
            return isSeller;
        } catch (Exception e) {
            logger.error("Error checking seller status: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates if the current user has access to modify a door.
     * Checks both ownership and role-based permissions.
     *
     * @param doorId ID of the door to validate
     * @throws AccessDeniedException if access is denied
     * @throws EntityNotFoundException if door not found
     */
    public void validateDoorAccess(Long doorId) {
        try {
            logger.info("Validating door access for door ID: {}", doorId);
            Door door = doorRepository.findById(doorId)
                .orElseThrow(() -> new EntityNotFoundException("Door not found"));
            
            User currentUser = userService.getCurrentUser();
            
            if (!userService.isAdmin(currentUser) && 
                !door.getSeller().getId().equals(currentUser.getId())) {
                logger.warn("Access denied for user {} to door {}", currentUser.getId(), doorId);
                throw new AccessDeniedException("You don't have permission to access this door");
            }
            
            logger.debug("Access validated for user {} to door {}", currentUser.getId(), doorId);
        } catch (Exception e) {
            logger.error("Error validating door access: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a door is currently locked.
     *
     * @param doorId ID of the door to check
     * @return true if door is locked, false otherwise
     * @throws EntityNotFoundException if door not found
     */
    public boolean isDoorLocked(Long doorId) {
        try {
            logger.info("Checking lock status for door ID: {}", doorId);
            Door door = doorRepository.findById(doorId)
                .orElseThrow(() -> new EntityNotFoundException("Door not found"));
            
            logger.debug("Door {} is {}", doorId, door.isLocked() ? "locked" : "unlocked");
            return door.isLocked();
        } catch (Exception e) {
            logger.error("Error checking door lock status: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates security configuration for a door.
     * Checks if security settings meet minimum requirements.
     *
     * @param door Door to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validateSecurityConfig(Door door) {
        try {
            logger.info("Validating security configuration for door ID: {}", door.getId());
            
            // Validate security settings
            if (door.getSecurityLevel() == null) {
                throw new IllegalArgumentException("Security level must be specified");
            }
            
            // Add more security validations as needed
            
            logger.debug("Security configuration validated for door {}", door.getId());
        } catch (Exception e) {
            logger.error("Error validating security configuration: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if emergency access is allowed for a door.
     * Used in emergency situations to override normal access controls.
     *
     * @param doorId ID of the door to check
     * @param emergencyCode Emergency access code
     * @return true if emergency access is granted, false otherwise
     */
    public boolean allowEmergencyAccess(Long doorId, String emergencyCode) {
        try {
            logger.info("Checking emergency access for door ID: {}", doorId);
            Door door = doorRepository.findById(doorId)
                .orElseThrow(() -> new EntityNotFoundException("Door not found"));
            
            // Implement emergency access logic
            boolean accessGranted = validateEmergencyCode(door, emergencyCode);
            
            if (accessGranted) {
                logger.warn("Emergency access granted for door {}", doorId);
            } else {
                logger.warn("Emergency access denied for door {}", doorId);
            }
            
            return accessGranted;
        } catch (Exception e) {
            logger.error("Error checking emergency access: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Validates an emergency access code.
     * Private helper method for emergency access validation.
     *
     * @param door Door to validate against
     * @param emergencyCode Code to validate
     * @return true if code is valid, false otherwise
     */
    private boolean validateEmergencyCode(Door door, String emergencyCode) {
        // Implement emergency code validation logic
        // This is a placeholder - implement actual validation
        return emergencyCode != null && emergencyCode.equals(door.getEmergencyCode());
    }
}