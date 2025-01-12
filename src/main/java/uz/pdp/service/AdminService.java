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
import uz.pdp.repository.EmailVerificationRepository;
import uz.pdp.service.EmailService;
import uz.pdp.entity.EmailVerification;
import uz.pdp.enums.VerificationType;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private EmailService emailService;

    @PreAuthorize("hasRole('ADMIN')")
    public boolean approveSeller(SellerRequestDto sellerRequestDto) {
        try {
            User user = userRepository.findById(sellerRequestDto.getUserId())
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
}
