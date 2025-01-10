package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.pdp.dto.SellerRequestDto;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.repository.UserRepository;
import uz.pdp.service.EmailService;

@Service
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public boolean approveSeller(SellerRequestDto sellerRequestDto) {
        try {
            User user = userRepository.findById(sellerRequestDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setRole(Role.SELLER);
            user.setSellerRequestPending(false);
            userRepository.save(user);
            emailService.sendVerificationEmail(user.getEmail());
            logger.info("User approved as seller: {}", user.getEmail());
            return true;
        } catch (Exception e) {
            logger.error("Error approving user as seller: {}", e.getMessage());
            return false;
        }
    }
}
