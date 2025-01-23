package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import uz.pdp.exception.UnauthorizedException;
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
     * 🎭 The Final Act of Seller Transformation! 
     * 
     * After the user has proven they're not a door-to-door spam bot by verifying their email,
     * this magical method lets admins bestow upon them the coveted SELLER role.
     * 
     * Technical Process:
     * 1. 🔍 Validate user existence and current role
     * 2. ✉️ Verify that email verification is complete
     * 3. 👑 Grant the seller crown
     * 4. 📨 Send the "Welcome to the Door Club" email
     * 
     * Note: Even doors need proper authentication - we can't just let any plank of wood in! 🚪
     *
     * @param userId ID of the aspiring door merchant
     * @param approved Whether the admin approves the seller request
     * @return Success message or reasons for rejection
     * @throws ResourceNotFoundException if the user is playing hide and seek
     * @throws ForbiddenException if they're trying to sneak through the back door
     * @throws BadRequestException if email isn't verified or other validation fails
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public EntityResponse<Void> processSellerRequest(Long userId, boolean approved) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Check if user has a pending seller request
            if (!user.isSellerRequestPending()) {
                logger.error("No pending seller request for user: {}", user.getUsername());
                throw new BadRequestException("No pending seller request found");
            }

            // Check if user has verified their email for seller request
            Optional<EmailVerification> verification = emailVerificationRepository
                    .findByUserIdAndTypeAndVerifiedTrueAndExpiryTimeAfter(
                            userId,
                            VerificationType.SELLER_REQUEST,
                            LocalDateTime.now());

            if (verification.isEmpty()) {
                logger.error("Email not verified for user: {}", user.getUsername());
                throw new BadRequestException("User must verify their email before seller request can be processed");
            }

            if (user.getRole() == Role.SELLER) {
                throw new ForbiddenException("User is already a seller");
            }

            // Process the admin's decision
            if (approved) {
                user.setRole(Role.SELLER);
                user.setSellerRequestPending(false);
                userRepository.save(user);

                // Send approval email
                emailService.sendHtmlEmail(
                    user.getEmail(),
                    "🎉 Welcome to the Door Sellers Club!",
                    String.format(
                        "<div style='font-family: Arial, sans-serif;'>" +
                        "<h1>🚪 Welcome to the Club!</h1>" +
                        "<p>Dear %s,</p>" +
                        "<p>Great news! Your seller account has been approved. You can now start listing your amazing doors!</p>" +
                        "<p>Remember:</p>" +
                        "<ul>" +
                        "<li>Quality photos make doors look their best</li>" +
                        "<li>Accurate descriptions help buyers find their perfect match</li>" +
                        "<li>Prompt responses lead to happy customers</li>" +
                        "</ul>" +
                        "<p>Now go forth and let your door business flourish! 🌟</p>" +
                        "<p>Best regards,<br>The Door Paradise Team</p>" +
                        "</div>",
                        user.getName()
                    )
                );
                logger.info("User approved as seller: {}", user.getUsername());
                return EntityResponse.success("User approved as seller successfully");
            } else {
                // If rejected, reset the pending status
                user.setSellerRequestPending(false);
                userRepository.save(user);

                // Send rejection email
                emailService.sendHtmlEmail(
                    user.getEmail(),
                    "Update on Your Seller Application",
                    String.format(
                        "<div style='font-family: Arial, sans-serif;'>" +
                        "<h1>Your Seller Application Update</h1>" +
                        "<p>Dear %s,</p>" +
                        "<p>We have reviewed your application to become a seller on Door Paradise. " +
                        "Unfortunately, we cannot approve your request at this time.</p>" +
                        "<p>You are welcome to apply again after 30 days with:</p>" +
                        "<ul>" +
                        "<li>Updated business information</li>" +
                        "<li>More detailed seller profile</li>" +
                        "<li>Additional documentation if required</li>" +
                        "</ul>" +
                        "<p>If you have any questions, please contact our support team.</p>" +
                        "<p>Best regards,<br>The Door Paradise Team</p>" +
                        "</div>",
                        user.getName()
                    )
                );
                logger.info("Seller request rejected for user: {}", user.getUsername());
                return EntityResponse.success("Seller request rejected successfully");
            }
        } catch (Exception e) {
            logger.error("Error processing seller request: {}", e.getMessage());
            throw new RuntimeException("Failed to process seller request: " + e.getMessage());
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

    /**
     * 👑 The Royal Role Reassignment Ceremony 👑
     * 
     * Changes a user's role with proper validation and notification.
     * It's like musical chairs, but with user roles and more paperwork!
     * 
     * Technical Process:
     * 1. 🔍 Validate user existence
     * 2. 🎭 Check role compatibility
     * 3. 👔 Update role
     * 4. 📨 Send notification
     * 
     * Note: Changing roles is like changing doors - make sure you have the right key! 🔑
     *
     * @param userId ID of the user getting a role makeover
     * @param newRole The shiny new role they'll be wearing
     * @return Success message or reasons for rejection
     * @throws ResourceNotFoundException if the user has vanished
     * @throws BadRequestException if the role change is invalid
     * @throws ForbiddenException if trying to demote the last admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public EntityResponse<Void> changeUserRole(Long userId, Role newRole) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Check if trying to change own role
            User currentAdmin = getCurrentUser();
            if (currentAdmin.getId().equals(userId)) {
                throw new ForbiddenException("Admins cannot change their own role");
            }

            // Prevent demoting the last admin
            if (user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
                long adminCount = userRepository.countByRole(Role.ADMIN);
                if (adminCount <= 1) {
                    throw new ForbiddenException("Cannot demote the last admin");
                }
            }

            // If demoting from SELLER to USER, handle seller-specific cleanup
            if (user.getRole() == Role.SELLER && newRole == Role.USER) {
                // Get all doors with a large page size to ensure we get all of them
                Page<Door> doorPage = doorRepository.findBySellerId(userId, 
                    PageRequest.of(0, Integer.MAX_VALUE));
                for (Door door : doorPage.getContent()) {
                    door.setActive(false);
                    doorRepository.save(door);
                }
            }

            // Update the role
            Role oldRole = user.getRole();
            user.setRole(newRole);
            userRepository.save(user);

            // Send notification email
            String emailSubject = "Your Role Has Been Updated";
            String emailContent = String.format(
                "<div style='font-family: Arial, sans-serif;'>" +
                "<h1>🔄 Role Update Notification</h1>" +
                "<p>Dear %s,</p>" +
                "<p>Your account role has been changed from <strong>%s</strong> to <strong>%s</strong>.</p>" +
                "<p>This change is effective immediately.</p>" +
                "%s" + // Additional info based on role change
                "<p>If you have any questions, please contact our support team.</p>" +
                "<p>Best regards,<br>The Door Paradise Team</p>" +
                "</div>",
                user.getName(),
                oldRole,
                newRole,
                getRoleChangeMessage(oldRole, newRole)
            );
            emailService.sendHtmlEmail(user.getEmail(), emailSubject, emailContent);

            logger.info("User role changed - ID: {}, Old Role: {}, New Role: {}", userId, oldRole, newRole);
            return EntityResponse.success(String.format("User role successfully changed from %s to %s", oldRole, newRole));

        } catch (Exception e) {
            logger.error("Error changing user role - ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to change user role: " + e.getMessage());
        }
    }

    /**
     * Gets a friendly message explaining the implications of a role change.
     * Because sometimes users need a little extra explanation! 📝
     */
    private String getRoleChangeMessage(Role oldRole, Role newRole) {
        if (oldRole == Role.SELLER && newRole == Role.USER) {
            return "<p><strong>Note:</strong> Your seller privileges have been removed. " +
                   "Any active door listings have been deactivated.</p>";
        } else if (newRole == Role.SELLER) {
            return "<p><strong>Congratulations!</strong> You can now list and sell doors on our platform. " +
                   "Check out our seller guidelines to get started!</p>";
        } else if (newRole == Role.ADMIN) {
            return "<p><strong>Welcome to the admin team!</strong> " +
                   "With great power comes great responsibility... and access to all the door puns! 🚪</p>";
        }
        return "";
    }

    /**
     * Gets the currently authenticated admin user.
     * Because even admins need to prove who they are! 🎭
     *
     * @return The authenticated admin user
     * @throws UnauthorizedException if no admin is logged in
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + username));
    }
}
