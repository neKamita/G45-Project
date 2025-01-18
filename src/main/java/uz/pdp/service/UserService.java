package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uz.pdp.payload.*;
import uz.pdp.entity.EmailVerification;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.enums.VerificationType;
import uz.pdp.repository.EmailVerificationRepository;
import uz.pdp.repository.UserRepository;
import uz.pdp.dto.*;
import uz.pdp.exception.*;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

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
    private boolean isAdmin(User user) {
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
    public EntityResponse<String> sendVerificationEmail(String email, VerificationType type) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email address cannot be empty");
        }

        try {
            String code = generateVerificationCode();
            emailService.sendVerificationEmail(email, code, type);

            EmailVerification verification = new EmailVerification();
            verification.setEmail(email);
            verification.setCode(code);
            verification.setType(type);
            verification.setExpiryTime(LocalDateTime.now().plusMinutes(15));
            emailVerificationRepository.save(verification);

            return EntityResponse.success("Verification email sent successfully");
        } catch (Exception e) {
            throw new BadRequestException("Failed to send verification email", e);
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
    public EntityResponse<User> requestSeller(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (user.isSellerRequestPending()) {
            throw new ConflictException("Seller request is already pending");
        }

        if (isSeller(user)) {
            throw new ConflictException("User is already a seller");
        }

        user.setSellerRequestPending(true);
        userRepository.save(user);

        sendVerificationEmail(user.getEmail(), VerificationType.SELLER_VERIFICATION);

        return EntityResponse.success("Seller request initiated successfully", user);
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
    public EntityResponse<User> verifySellerEmail(Long userId, String verificationCode) {
        if (hasExceededFailedAttempts(userId)) {
            throw new ForbiddenException("Too many failed attempts. Please try again later.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!user.isSellerRequestPending()) {
            throw new BadRequestException("No pending seller request found");
        }

        EmailVerification verification = emailVerificationRepository
                .findByEmailAndTypeAndCodeAndExpiryTimeAfter(
                        user.getEmail(),
                        VerificationType.SELLER_VERIFICATION,
                        verificationCode,
                        LocalDateTime.now())
                .orElseThrow(() -> {
                    recordFailedAttempt(userId);
                    throw new BadRequestException("Invalid or expired verification code");
                });

        user.setRole(Role.SELLER);
        user.setSellerRequestPending(false);
        userRepository.save(user);

        emailVerificationRepository.delete(verification);
        clearFailedAttempts(userId);

        return EntityResponse.success("Seller verification successful", user);
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
}
