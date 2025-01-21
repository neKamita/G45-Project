package uz.pdp.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import uz.pdp.repository.UserRepository;

/**
 * Custom user details service for authentication.
 * Fun fact: This service is like a bouncer at a club,
 * but instead of checking IDs, it checks names! 📧
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String name)
        throws UsernameNotFoundException {
        return userRepository
            .findByName(name)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "User not found with name: " + name
                )
            );
    }
}
