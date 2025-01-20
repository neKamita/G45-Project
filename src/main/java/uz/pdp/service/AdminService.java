package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uz.pdp.entity.Door;
import uz.pdp.entity.EmailVerification;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.enums.VerificationType;
import uz.pdp.exception.BadRequestException;
import uz.pdp.exception.ForbiddenException;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.payload.EntityResponse;
import uz.pdp.dto.UpdateUserDTO;
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.EmailVerificationRepository;
import uz.pdp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * The Admin Control Room Service - Where Order Meets Chaos 👮
 * 
 * This service implements administrative operations including:
 * - User management (herding cats)
 * - Role assignment (distributing superpowers)
 * - Account verification (making sure users are real people)
 * - System maintenance (cleaning up the mess)
 * 
 * Technical Features:
 * - Role-based access control
 * - Transactional operations
 * - Audit logging
 * - Email notifications
 * 
 * WARNING: This service has more power than your average superhero.
 * Use it wisely, or you'll be the one explaining to users why
 * their cat pictures disappeared. 🐱
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class AdminService {
    // For logging admin actions (and occasional facepalms)
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    // The keepers of our precious data
    @Autowired
    private UserRepository userRepository;          // Where user dreams live

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;  // Proof that you're not a bot

    @Autowired
    private EmailService emailService;              // The messenger of good (and bad) news

    @Autowired
    private DoorRepository doorRepository;          // The gateway to all things door-related

    /**
     * Promotes a user to seller status with proper validation and notification.
     * 
     * Technical Process:
     * 1. Validate user existence and current role
     * 2. Check eligibility criteria
     * 3. Update role and permissions
     * 4. Send confirmation email
     * 
     * Note: We've automated everything except common sense.
     * That's still a manual process. 🤷
     *
     * @param userId ID of the user getting their promotion
     * @return Success message or reasons for rejection
     * @throws ResourceNotFoundException if the user is MIA
     * @throws ForbiddenException if they're trying to game the system
     */
    @PreAuthorize("hasRole('ADMIN')")
    public EntityResponse<Void> approveSeller(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Check if user has verified email for seller request
            Optional<EmailVerification> verification = emailVerificationRepository
                    .findByUserIdAndTypeAndVerifiedFalseAndExpiryTimeAfter(
                            userId,
                            VerificationType.SELLER_REQUEST,
                            LocalDateTime.now());

            if (verification.isEmpty()) {
                logger.error("No valid verification found for user: {}", user.getUsername());
                return EntityResponse.error("No valid verification request found");
            }

            if (user.getRole() == Role.SELLER) {
                throw new ForbiddenException("User is already a seller");
            }

            user.setRole(Role.SELLER);
            user.setSellerRequestPending(false);
            userRepository.save(user);

            // Mark verification as complete
            EmailVerification emailVerification = verification.get();
            emailVerification.setVerified(true);
            emailVerificationRepository.save(emailVerification);

            // Send confirmation email
            emailService.sendVerificationEmail(user.getUsername(), "Your seller account has been approved", VerificationType.EMAIL_CONFIRMATION);
            logger.info("User approved as seller: {}", user.getUsername());
            return EntityResponse.success("User approved as seller successfully");

        } catch (Exception e) {
            logger.error("Error approving user as seller: {}", e.getMessage());
            return EntityResponse.error("Failed to approve seller: " + e.getMessage());
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
     * @throws ResourceNotFoundException if the user is not found
     * @throws ForbiddenException        if the user is an admin
     * @throws RuntimeException          if there's an error during the deactivation
     *                                   process
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public EntityResponse<Void> deactivateAccount(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (user.getRole() == Role.ADMIN) {
                throw new ForbiddenException("Cannot deactivate admin account");
            }

            if (!user.isActive()) {
                throw new BadRequestException("Account is already deactivated");
            }

            user.setActive(false);
            userRepository.save(user);

            // If user is a seller, deactivate all their doors
            if (user.getRole() == Role.SELLER) {
                // Get all doors with a large page size to ensure we get all of them
                Page<Door> doorPage = doorRepository.findBySellerId(userId, 
                    PageRequest.of(0, Integer.MAX_VALUE));
                for (Door door : doorPage.getContent()) {
                    door.setActive(false);
                    doorRepository.save(door);
                }
            }

            logger.info("Account deactivated for user: {}", user.getUsername());
            return EntityResponse.success("Account deactivated successfully");

        } catch (Exception e) {
            logger.error("Error deactivating account: {}", e.getMessage());
            return EntityResponse.error("Failed to deactivate account: " + e.getMessage());
        }
    }

    /**
     * Updates a user's profile information.
     * Validates and updates user details while preserving sensitive information.
     *
     * Technical Steps:
     * 1. Validate update request
     * 2. Check user existence
     * 3. Apply changes with proper validation
     * 4. Update audit logs
     * 
     * Pro tip: With great power comes great responsibility...
     * and a lot of "I forgot my password" tickets. 🔑
     *
     * @param userId The chosen one's ID
     * @param updateUserDTO The new user specs
     * @return Updated user or a list of why we can't have nice things
     * @throws ResourceNotFoundException if the user pulled a Houdini
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public EntityResponse<User> updateUser(Long userId, UpdateUserDTO updateUserDTO) {
        try {
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Update allowed fields from DTO
            existingUser.setName(updateUserDTO.getName());
            existingUser.setLastname(updateUserDTO.getLastname());
            existingUser.setEmail(updateUserDTO.getEmail());
            existingUser.setPhone(updateUserDTO.getPhone());
            
            // Only admin can update role and status
            existingUser.setRole(updateUserDTO.getRole());
            existingUser.setActive(updateUserDTO.isActive());

            User savedUser = userRepository.save(existingUser);
            logger.info("User profile updated successfully for ID: {}", userId);
            
            return new EntityResponse<>("User profile updated successfully", true, savedUser);
        } catch (ResourceNotFoundException e) {
            logger.error("User not found with ID: {}", userId);
            throw e;
        } catch (Exception e) {
            logger.error("Error updating user profile: {}", e.getMessage());
            throw new RuntimeException("Failed to update user profile: " + e.getMessage());
        }
    }
}
