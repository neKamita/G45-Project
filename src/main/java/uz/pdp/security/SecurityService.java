package uz.pdp.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service to handle security-related operations.
 * The bouncer of our door shop! 🚪
 */
@Slf4j
@Service
public class SecurityService {

    /**
     * Check if the current user is authenticated.
     * Like checking VIP passes at the door! 
     *
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null && 
                   authentication.isAuthenticated() && 
                   !"anonymousUser".equals(authentication.getPrincipal());
        } catch (Exception e) {
            log.error("Error checking authentication status: {}", e.getMessage());
            return false;
        }
    }
}
