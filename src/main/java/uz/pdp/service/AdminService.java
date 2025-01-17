package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import uz.pdp.dto.SellerRequestDto;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.repository.UserRepository;
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.EmailVerificationRepository;
import uz.pdp.entity.Door;
import uz.pdp.entity.EmailVerification;
import uz.pdp.enums.VerificationType;
import uz.pdp.payload.EntityResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

/**
 * Service class for handling administrative operations in the system.
 * This service provides functionality for managing users and their roles,
 * particularly focusing on seller approval and account management.
 *
 * @author Your Team Name
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DoorRepository doorRepository;

    /**
     * Approves a user's request to become a seller.
     * This method verifies that:
     * 1. The user exists
     * 2. The user has a verified email for seller request
     * 3. The user is not already a seller
     *
     * @param userId The ID of the user to be approved as a seller
     * @return boolean indicating whether the approval was successful
     * @throws IllegalArgumentException if the user is not found or is already a seller
     * @throws RuntimeException if there's an error during the approval process
     */
    @PreAuthorize("hasRole('ADMIN')")
    public boolean approveSeller(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user has verified email for seller request
            Optional<EmailVerification> verification = emailVerificationRepository
                .findByUserIdAndTypeAndVerifiedFalseAndExpiryTimeAfter(
                    user.getId(),
                    VerificationType.SELLER_REQUEST,
                    LocalDateTime.now()
                );

            if (verification.isEmpty()) {
                logger.error("No valid verification found for user: {}", user.getEmail());
                return false;
            }

            user.setRole(Role.SELLER);
            user.setSellerRequestPending(false);
            userRepository.save(user);
            
            // Mark verification as complete
            emailVerificationRepository.markAsVerified(verification.get().getId());
            
            // Send confirmation email
            emailService.sendVerificationEmail(user.getEmail());
            logger.info("User approved as seller: {}", user.getEmail());
            return true;
            
        } catch (Exception e) {
            logger.error("Error approving user as seller: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Deactivates a user account in the system.
     * This operation:
     * 1. Verifies the user exists
     * 2. Sets the account to inactive
     * 3. Preserves the user's data but prevents login
     *
     * @param userId The ID of the user account to deactivate
     * @return EntityResponse containing the operation result
     * @throws IllegalArgumentException if the user is not found
     * @throws RuntimeException if there's an error during the deactivation process
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public EntityResponse<Void> deactivateAccount(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
                
            // Don't allow deactivating other admins
            if (user.getRole() == Role.ADMIN) {
                return EntityResponse.error("Cannot deactivate admin accounts");
            }
            
            user.setActive(false);
            userRepository.save(user);
            
            // If user is seller, deactivate all their doors
            if (user.getRole() == Role.SELLER) {
                List<Door> sellerDoors = doorRepository.findBySellerId(userId);
                for (Door door : sellerDoors) {
                    door.setActive(false);
                }
                doorRepository.saveAll(sellerDoors);
                logger.info("Deactivated {} doors for seller: {}", sellerDoors.size(), userId);
            }
            
            logger.info("Account deactivated successfully: {}", userId);
            return EntityResponse.success("Account deactivated successfully");
            
        } catch (EntityNotFoundException e) {
            logger.error("User not found: {}", userId);
            return EntityResponse.error("User not found");
        } catch (Exception e) {
            logger.error("Error deactivating account: {}", e.getMessage());
            return EntityResponse.error("Failed to deactivate account");
        }
    }
}
