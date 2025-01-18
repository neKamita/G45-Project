package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.entity.EmailVerification;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.enums.VerificationType;
import uz.pdp.exception.BadRequestException;
import uz.pdp.exception.ConflictException;
import uz.pdp.exception.ForbiddenException;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.exception.UnauthorizedException;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.EmailVerificationRepository;
import uz.pdp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * Service class for managing user-related operations.
 * Handles user profile management, role management, and email verification.
 * Implements security measures including rate limiting and verification
 * attempts tracking.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
@Slf4j
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String FAILED_ATTEMPTS_PREFIX = "verification_attempts:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 15; // minutes

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RedisTemplate<String, Integer> redisTemplate;

    @Autowired
    public UserService(UserRepository userRepository,
            EmailService emailService,
            EmailVerificationRepository emailVerificationRepository,
            RedisTemplate<String, Integer> redisTemplate) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.emailVerificationRepository = emailVerificationRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return User the current user's details
     * @throws UnauthorizedException if no user is authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user found");
        }

        String username = authentication.getName();
        return userRepository.findByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    /**
     * Checks if a user has seller privileges.
     *
     * @param user User to check
     * @return true if user is a seller, false otherwise
     * @throws UnauthorizedException if user is not authenticated
     */
    private boolean isSeller(User user) {
        if (user == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return user.getRole() == Role.SELLER;
    }

    /**
     * Checks if a user has admin privileges.
     *
     * @param user User to check
     * @return true if user is an admin, false otherwise
     * @throws UnauthorizedException if user is not authenticated
     */
    public boolean isAdmin(User user) {
        if (user == null) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return user.getRole() == Role.ADMIN;
    }

    /**
     * Sends a verification email to the user.
     *
     * @param email User's email address
     * @param type  Type of verification
     * @return EntityResponse indicating success/failure
     * @throws BadRequestException if email address is empty
     */
    public ResponseEntity<EntityResponse<String>> sendVerificationEmail(String email, VerificationType type) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email address cannot be empty");
        }

        try {
            String code = generateVerificationCode();
            emailService.sendVerificationEmail(email, code, type);

            EmailVerification verification = new EmailVerification();
            verification.setUser(getCurrentUser());
            verification.setVerificationCode(code);
            verification.setType(type);
            verification.setExpiryTime(LocalDateTime.now().plusMinutes(15));
            emailVerificationRepository.save(verification);

            return ResponseEntity.ok(EntityResponse.success("Verification email sent successfully"));
        } catch (Exception e) {
            logger.error("Error sending verification email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(EntityResponse.error("Failed to send verification email: " + e.getMessage()));
        }
    }

    /**
     * Initiates the process for a user to become a seller.
     * Sends verification email and creates verification record.
     *
     * @param userId ID of the user requesting seller status
     * @return EntityResponse containing updated user details
     * @throws ResourceNotFoundException if user not found
     * @throws ConflictException         if seller request is already pending or
     *                                   user is already a seller
     */
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public EntityResponse<User> requestSeller(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (user.getRole() == Role.SELLER) {
                throw new ConflictException("User is already a seller");
            }

            if (user.isSellerRequestPending()) {
                throw new ConflictException("Seller request is already pending");
            }

            user.setSellerRequestPending(true);
            User savedUser = userRepository.save(user);

            // Send verification email
            sendVerificationEmail(user.getEmail(), VerificationType.SELLER_REQUEST);

            logger.info("Seller request initiated for user ID: {}", userId);
            return EntityResponse.success("Seller request initiated successfully", savedUser);
        } catch (ResourceNotFoundException | ConflictException e) {
            logger.error("Error processing seller request - ID {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error processing seller request for user ID {}: {}", userId, e.getMessage());
            throw new BadRequestException("Failed to process seller request: " + e.getMessage());
        }
    }

    /**
     * Verifies seller email with provided verification code.
     * Implements rate limiting and attempt tracking.
     *
     * @param userId           ID of the user
     * @param verificationCode Code to verify
     * @return EntityResponse containing updated user details
     * @throws ResourceNotFoundException if user not found
     * @throws ForbiddenException        if too many failed attempts
     * @throws BadRequestException       if no pending seller request found or
     *                                   invalid/expired verification code
     */
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public EntityResponse<User> verifySellerEmail(Long userId, String verificationCode) {
        try {
            if (hasExceededFailedAttempts(userId)) {
                throw new ForbiddenException("Too many failed attempts. Please try again later.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!user.isSellerRequestPending()) {
                throw new BadRequestException("No pending seller request found");
            }

            Optional<EmailVerification> verification = emailVerificationRepository
                    .findByUserIdAndTypeAndVerifiedFalseAndExpiryTimeAfter(
                            userId,
                            VerificationType.SELLER_REQUEST,
                            LocalDateTime.now());

            if (verification.isEmpty()) {
                throw new BadRequestException("No valid verification request found");
            }

            EmailVerification emailVerification = verification.get();
            if (!emailVerification.getVerificationCode().equals(verificationCode)) {
                recordFailedAttempt(userId);
                throw new BadRequestException("Invalid verification code");
            }

            emailVerification.setVerified(true);
            emailVerificationRepository.save(emailVerification);

            user.setSellerRequestPending(false);
            user.setRole(Role.SELLER);
            User savedUser = userRepository.save(user);

            clearFailedAttempts(userId);
            logger.info("Seller email verified for user ID: {}", userId);
            return EntityResponse.success("Email verified successfully", savedUser);
        } catch (ResourceNotFoundException | ForbiddenException | BadRequestException e) {
            logger.error("Error verifying seller email - ID {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error verifying seller email for user ID {}: {}", userId, e.getMessage());
            throw new BadRequestException("Failed to verify seller email: " + e.getMessage());
        }
    }

    /**
     * Retrieves all users in the system.
     * Requires admin privileges.
     *
     * @return EntityResponse containing list of all users
     */
    @PreAuthorize("hasRole('ADMIN')")
    public EntityResponse<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return EntityResponse.success("Users retrieved successfully", users);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id ID of the user to retrieve
     * @return EntityResponse containing user details
     * @throws ResourceNotFoundException if user not found
     */
    @PreAuthorize("hasRole('ADMIN')")
    public EntityResponse<User> getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return EntityResponse.success("User retrieved successfully", user);
    }

    /**
     * Checks if a user has exceeded maximum failed verification attempts.
     *
     * @param userId ID of the user to check
     * @return true if attempts exceeded, false otherwise
     */
    private boolean hasExceededFailedAttempts(Long userId) {
        String key = FAILED_ATTEMPTS_PREFIX + userId;
        Integer attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && attempts >= MAX_FAILED_ATTEMPTS;
    }

    /**
     * Records a failed verification attempt for a user.
     *
     * @param userId ID of the user
     */
    private void recordFailedAttempt(Long userId) {
        String key = FAILED_ATTEMPTS_PREFIX + userId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOCKOUT_DURATION, TimeUnit.MINUTES);
    }

    /**
     * Clears failed verification attempts for a user.
     *
     * @param userId ID of the user
     */
    private void clearFailedAttempts(Long userId) {
        String key = FAILED_ATTEMPTS_PREFIX + userId;
        redisTemplate.delete(key);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    /**
     * Updates a user's profile information.
     * Validates and updates user details while preserving sensitive information.
     *
     * @param id User ID to update
     * @param updatedUser Updated user details
     * @return EntityResponse containing updated user
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public EntityResponse<User> updateProfile(Long id, @Valid User updatedUser) {
        try {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Update only allowed fields
            existingUser.setName(updatedUser.getName());
            existingUser.setLastname(updatedUser.getLastname());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone(updatedUser.getPhone());
            
            // Preserve sensitive information
            existingUser.setPassword(existingUser.getPassword());
            existingUser.setRole(existingUser.getRole());
            existingUser.setActive(existingUser.isActive());
            existingUser.setSellerRequestPending(existingUser.isSellerRequestPending());
            
            User savedUser = userRepository.save(existingUser);
            logger.info("Successfully updated profile for user ID: {}", id);
            return EntityResponse.success("Profile updated successfully", savedUser);
        } catch (ResourceNotFoundException e) {
            logger.error("User not found for profile update - ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error updating profile for user ID {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to update profile: " + e.getMessage());
        }
    }

    /**
     * Deactivates a user account.
     * Sets the account status to inactive and logs the action.
     *
     * @param id User ID to deactivate
     * @return EntityResponse indicating success/failure
     * @throws ResourceNotFoundException if user not found
     * @throws ForbiddenException if user is an admin
     */
    @Transactional
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public EntityResponse<Void> deactivateAccount(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (user.getRole() == Role.ADMIN) {
                throw new ForbiddenException("Admin accounts cannot be deactivated");
            }

            user.setActive(false);
            userRepository.save(user);
            
            logger.info("Successfully deactivated account for user ID: {}", id);
            return EntityResponse.success("Account deactivated successfully");
        } catch (ResourceNotFoundException | ForbiddenException e) {
            logger.error("Error deactivating account - ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deactivating account for user ID {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to deactivate account: " + e.getMessage());
        }
    }
}
