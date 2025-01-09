package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

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
}
