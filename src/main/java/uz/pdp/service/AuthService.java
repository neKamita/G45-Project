package uz.pdp.service;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import uz.pdp.config.filtr.JwtProvider;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    public ResponseEntity<?> signIn(@Valid SignInRequest signInRequest) {
        Optional<User> userOptional = userRepository.findByName(signInRequest.getName());
        if (userOptional.isEmpty() || !passwordEncoder.matches(signInRequest.getPassword(), userOptional.get().getPassword())) {
            logger.warn("Authentication failed for user: {}", signInRequest.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid name or password"));
        }

        String token = jwtProvider.generateToken(userOptional.get().getName());
        logger.info("User {} successfully signed in.", signInRequest.getName());
        return ResponseEntity.ok(Map.of(
            "message", "Successfully signed in",
            "token", token
        ));
    }

    public ResponseEntity<?> signUp(@Valid SignUpRequest signUpRequest) {
        if (userRepository.findByName(signUpRequest.getName()).isPresent()) {
            logger.warn("Registration failed: Name '{}' is already taken.", signUpRequest.getName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Name is already taken"));
        }
    
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Email is already taken"));
        }
    
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setLastname(signUpRequest.getLastname());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        logger.info("User '{}' registered successfully.", signUpRequest.getName());
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }
}