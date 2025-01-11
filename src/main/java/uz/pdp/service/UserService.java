package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
import java.util.Random;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private final EmailService emailService;
    private final EmailVerificationRepository emailVerificationRepository;

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

        // Check if already a seller
        if (user.getRole() == Role.SELLER) {
            return EntityResponse.error("User is already a seller", null);
        }

        // Check for existing verification
        if (emailVerificationRepository.existsByUserIdAndTypeAndVerifiedFalseAndExpiryTimeAfter(
                user.getId(), VerificationType.SELLER_REQUEST, LocalDateTime.now())) {
            return EntityResponse.error("Verification already sent. Please check your email", null);
        }

        // Send verification email using user's email from User entity
        EntityResponse<String> emailResponse = emailService.sendVerificationEmail(user.getEmail());
        if (!emailResponse.success()) {
            return EntityResponse.error(emailResponse.message(), null);
        }

        // Generate verification code and create email verification
        String verificationCode = generateVerificationCode();
        EmailVerification emailVerification = new EmailVerification();
        emailVerification.setUser(user);
        emailVerification.setVerificationCode(verificationCode);
        emailVerification.setExpiryTime(LocalDateTime.now().plusMinutes(15));
        emailVerification.setType(VerificationType.SELLER_REQUEST);
        emailVerificationRepository.save(emailVerification);

        user.setSellerRequestPending(true);
        userRepository.save(user);

        return EntityResponse.success("Verification code sent to your email", user);
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
