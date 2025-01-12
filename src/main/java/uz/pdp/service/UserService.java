package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import uz.pdp.payload.*;
import uz.pdp.entity.EmailVerification;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.enums.VerificationType;
import uz.pdp.repository.EmailVerificationRepository;
import uz.pdp.repository.UserRepository;
import uz.pdp.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    private static final String FAILED_ATTEMPTS_PREFIX = "verification_attempts:";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 15; // minutes

    Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, 
                      EmailService emailService,
                      EmailVerificationRepository emailVerificationRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No authenticated user found");
        }
        
        String username = authentication.getName();
        return userRepository.findByName(username)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    public boolean isSeller(User user) {
        return user.getRole() == Role.SELLER;
    }

    public boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    public EntityResponse<User> requestSeller(@Valid SellerRequestDto sellerRequestDto) {
        User user = userRepository.findById(sellerRequestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() == Role.ADMIN) {
            return EntityResponse.error("Admins cannot request to become sellers", null);
        }

        if (user.getRole() == Role.SELLER) {
            return EntityResponse.error("User is already a seller", null);
        }

        String verificationCode = generateVerificationCode();
        
        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setUser(user);
        emailVerification.setVerificationCode(verificationCode);
        emailVerification.setExpiryTime(LocalDateTime.now().plusMinutes(15));
        emailVerification.setType(VerificationType.SELLER_REQUEST);
        emailVerificationRepository.save(emailVerification);

        EntityResponse<String> emailResponse = emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        if (!emailResponse.success()) {
            return EntityResponse.error(emailResponse.message(), null);
        }

        user.setSellerRequestPending(true);
        userRepository.save(user);

        return EntityResponse.success("Verification code sent to your email", user);
    }

    @Transactional
    public EntityResponse<User> verifySellerEmail(Long userId, String verificationCode) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            // Check for too many failed attempts
            if (hasExceededFailedAttempts(userId)) {
                return EntityResponse.error("Too many failed attempts. Please try again later.");
            }

            Optional<EmailVerification> verification = emailVerificationRepository
                .findByUserIdAndVerificationCodeAndTypeAndVerifiedFalseAndExpiryTimeAfter(
                    userId,
                    verificationCode,
                    VerificationType.SELLER_REQUEST,
                    LocalDateTime.now()
                );

            if (verification.isEmpty()) {
                recordFailedAttempt(userId);
                return EntityResponse.error("Invalid or expired verification code");
            }

            EmailVerification emailVerification = verification.get();
            emailVerification.setVerified(true);
            emailVerificationRepository.save(emailVerification);

            clearFailedAttempts(userId);
            return EntityResponse.success("Email verified successfully", user);
        } catch (Exception e) {
            logger.error("Verification failed for user {}: {}", userId, e.getMessage());
            return EntityResponse.error("Verification failed: " + e.getMessage());
        }
    }

    private boolean hasExceededFailedAttempts(Long userId) {
        String key = FAILED_ATTEMPTS_PREFIX + userId;
        Integer attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && attempts >= MAX_FAILED_ATTEMPTS;
    }

    private void recordFailedAttempt(Long userId) {
        String key = FAILED_ATTEMPTS_PREFIX + userId;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, LOCKOUT_DURATION, TimeUnit.MINUTES);
    }

    private void clearFailedAttempts(Long userId) {
        String key = FAILED_ATTEMPTS_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public EntityResponse<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return EntityResponse.success("Users retrieved successfully", users);
    }

    public EntityResponse<User> getUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            return EntityResponse.success("User retrieved successfully", user);
        } catch (Exception e) {
            return EntityResponse.error("User not found", null);
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
