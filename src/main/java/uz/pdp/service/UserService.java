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
 * 🎪 The User Whisperer Service 🎪
 * 
 * Welcome to the user management circus! Where we juggle user requests,
 * balance security tightropes, and occasionally perform magic tricks with the database.
 *
 * 🎯 Technical Features:
 * - User CRUD operations (Create, Read, Update, Delete... if we dare)
 * - Profile management (because users love changing their minds)
 * - Security enforcement (keeping the bad guys out, hopefully)
 * - Session handling (because someone always forgets to log out)
 *
 * 🎭 Common User States:
 * - Active (actually using the system)
 * - Inactive (forgot we exist)
 * - Locked (tried 'password123' too many times)
 * - Confused (our most popular state)
 *
 * Remember: Users are like cats 🐱
 * - They don't read instructions
 * - They do unexpected things
 * - They blame you when things go wrong
 *
 * @version 2.0
 * @since 2025-01-18
 */
@Service
@Slf4j
public class UserService {

    // For logging user shenanigans
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    // Where we keep all our precious user data
    private final UserRepository userRepository;
    
    // For when users need to prove they own their email
    private final EmailVerificationRepository emailVerificationRepository;
    
    // Our trusty email delivery service
    private final EmailService emailService;

    // Cache for user sessions (because databases need naps too)
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
     * Gets the current user or throws a fit if they're not logged in
     * 
     * @return The user object (if they bothered to log in)
     * @throws UnauthorizedException when they try to be sneaky
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
     * 🎩 The Seller Transformation Magic ✨
     * 
     * Initiates the mystical process of transforming a regular user into a seller.
     * It's like a digital Cinderella story, but instead of a glass slipper,
     * we send a verification email!
     *
     * The Process:
     * 1. 🔍 Check if user exists (no ghosts allowed)
     * 2. 🎭 Verify they're not already a seller (no double transformations!)
     * 3. 📧 Send the magical verification email
     * 4. 🕒 Set their request as pending (the suspense!)
     *
     * @param userId ID of the user dreaming of seller stardom
     * @return EntityResponse with the user's updated details
     * @throws ResourceNotFoundException if user pulls a disappearing act
     * @throws ConflictException if they're already a seller or have a pending request
     */
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public EntityResponse<User> requestSeller(Long userId) {
        try {
            // 🔍 Find our aspiring seller
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // 🎭 Check if they're already part of the seller club
            if (user.getRole() == Role.SELLER) {
                throw new ConflictException("User is already a seller");
            }

            // 🔄 Check for pending requests (no double-clicking allowed!)
            if (user.isSellerRequestPending()) {
                throw new ConflictException("Seller request is already pending");
            }

            // 📝 Mark the transformation as in progress
            user.setSellerRequestPending(true);
            User savedUser = userRepository.save(user);

            // 📧 Send the magical verification email
            sendVerificationEmail(user.getEmail(), VerificationType.SELLER_REQUEST);

            // 📢 Announce the good news
            logger.info("Seller request initiated for user ID: {}", userId);
            return EntityResponse.success("Seller request initiated successfully", savedUser);
        } catch (ResourceNotFoundException | ConflictException e) {
            // 🚫 Handle expected issues gracefully
            logger.error("Error processing seller request - ID {}: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 💥 Something unexpected happened
            logger.error("Error processing seller request for user ID {}: {}", userId, e.getMessage());
            throw new BadRequestException("Failed to process seller request: " + e.getMessage());
        }
    }

    /**
     * Verifies seller email with provided verification code.
     * This is step 1 of the seller approval process.
     * After email verification, an admin must approve the request.
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

            // Mark email as verified but keep request pending for admin approval
            emailVerification.setVerified(true);
            emailVerificationRepository.save(emailVerification);

            // Send notification about successful verification and pending admin approval
            emailService.sendHtmlEmail(
                user.getEmail(),
                "Email Verified - Awaiting Admin Approval",
                String.format(
                    "<div style='font-family: Arial, sans-serif;'>" +
                    "<h1>🎉 Email Verification Successful!</h1>" +
                    "<p>Dear %s,</p>" +
                    "<p>Great news! Your email has been successfully verified.</p>" +
                    "<p><strong>What's Next?</strong></p>" +
                    "<p>Your seller request is now awaiting admin approval. This usually takes 1-2 business days.</p>" +
                    "<p>We'll notify you as soon as an admin reviews your request.</p>" +
                    "<p>Best regards,<br>The Door Paradise Team</p>" +
                    "</div>",
                    user.getName()
                )
            );

            clearFailedAttempts(userId);
            logger.info("Seller email verified for user ID: {}, awaiting admin approval", userId);
            return EntityResponse.success("Email verified successfully. Your request is now pending admin approval.", user);
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    public EntityResponse<User> getUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            return EntityResponse.success("User retrieved successfully", user);
        } catch (ResourceNotFoundException e) {
            logger.error("User not found - ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving user - ID {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to retrieve user: " + e.getMessage());
        }
    }

    /**
     * Checks if a user has exceeded maximum failed verification attempts.
     *
     * @param userId ID of the user to check
     * @return true if attempts exceeded, false otherwise
     */
    private boolean hasExceededFailedAttempts(Long userId) {
        String key = "verification_attempts:" + userId;
        Integer attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && attempts >= 5;
    }

    /**
     * Records a failed verification attempt for a user.
     *
     * @param userId ID of the user
     */
    private void recordFailedAttempt(Long userId) {
        String key = "verification_attempts:" + userId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, 15, TimeUnit.MINUTES);
    }

    /**
     * Clears failed verification attempts for a user.
     *
     * @param userId ID of the user
     */
    private void clearFailedAttempts(Long userId) {
        String key = "verification_attempts:" + userId;
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
