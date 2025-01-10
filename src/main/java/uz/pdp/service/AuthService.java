package uz.pdp.service;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import uz.pdp.config.filtr.JwtProvider;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.repository.UserRepository;
import uz.pdp.payload.*;

import java.time.LocalDateTime;
import java.util.HashMap;
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

    public ResponseEntity<EntityResponse<Map<String, String>>> signIn(@Valid SignInRequest signInRequest) {
        Optional<User> userOptional = userRepository.findByName(signInRequest.getName());
        if (userOptional.isEmpty() || !passwordEncoder.matches(signInRequest.getPassword(), userOptional.get().getPassword())) {
            logger.warn("Authentication failed for user: {}", signInRequest.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(EntityResponse.error("Invalid name or password"));
        }

        String token = jwtProvider.generateToken(userOptional.get().getName());
        logger.info("User {} successfully signed in.", signInRequest.getName());
        Map<String, String> response = Map.of("token", token);
        return ResponseEntity.ok(EntityResponse.success("Successfully signed in", response));
    }

    public ResponseEntity<EntityResponse<Map<String, String>>> signUp(@Valid SignUpRequest signUpRequest) {
        if (userRepository.findByName(signUpRequest.getName()).isPresent()) {
            logger.warn("Registration failed: Name '{}' is already taken.", signUpRequest.getName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error("Name is already taken"));
        }
    
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email '{}' is already taken.", signUpRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error("Email is already taken"));
        }
    
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setLastname(signUpRequest.getLastname());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        
        String token = jwtProvider.generateToken(user.getName());
        logger.info("User '{}' registered successfully and token generated.", signUpRequest.getName());
        
        Map<String, String> response = Map.of(
            "message", "User registered successfully",
            "token", token
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(EntityResponse.success("User registered successfully", response));
    }

    public EntityResponse<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            
        return EntityResponse.error("Validation failed", errors);
    }
}