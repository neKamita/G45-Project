package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.EmailVerificationRepository;
import uz.pdp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
     * @return EntityResponse containing the operation result
     * @throws ResourceNotFoundException if the user is not found
     * @throws ForbiddenException        if the user is already a seller
     * @throws RuntimeException          if there's an error during the approval
     *                                   process
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
            emailService.sendVerificationEmail(user.getUsername(), "Your seller account has been approved", VerificationType.SELLER_APPROVAL);
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
                List<Door> doors = doorRepository.findByUserId(userId);
                for (Door door : doors) {
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
}
