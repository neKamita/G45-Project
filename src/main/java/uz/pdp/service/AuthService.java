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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Autowired
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public ResponseEntity<EntityResponse<Map<String, String>>> signIn(@Valid SignInRequest signInRequest) {
        Optional<User> userOptional = userRepository.findByName(signInRequest.getName());
        if (userOptional.isEmpty() || !passwordEncoder.matches(signInRequest.getPassword(), userOptional.get().getPassword())) {
            logger.warn("Authentication failed for user: {}", signInRequest.getName());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(EntityResponse.error("Invalid name or password"));
        }
    
        String token = jwtProvider.generateToken(userOptional.get().getName());
        logger.info("User {} successfully signed in.", signInRequest.getName());
        
        // Return only the token in the data map
        Map<String, String> response = Map.of("token", token);
        return ResponseEntity.ok(EntityResponse.success("Successfully signed in", response));
    }

    public ResponseEntity<EntityResponse<Map<String, String>>> signUp(@Valid SignUpRequest signUpRequest) {
        try {
            // Validate request fields
            Map<String, String> validationErrors = validateSignUpFields(signUpRequest);
            if (!validationErrors.isEmpty()) {
                logger.warn("Registration validation failed: {}", validationErrors);
                return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Validation failed", validationErrors));
            }

            // Check for existing user
            if (userRepository.findByName(signUpRequest.getName()).isPresent()) {
                logger.warn("Registration failed: Username already exists - {}", signUpRequest.getName());
                return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Username is already taken"));
            }

            if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
                logger.warn("Registration failed: Email already exists - {}", signUpRequest.getEmail());
                return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Email is already taken"));
            }

            // Create new user
            User user = new User();
            user.setName(signUpRequest.getName());
            user.setLastname(signUpRequest.getLastname());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRole(Role.USER);
            
            userRepository.save(user);
            String token = jwtProvider.generateToken(user.getName());
            logger.info("User registered successfully: {}", user.getName());

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.success("User registered successfully", 
                    Map.of("token", token)));

        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    private Map<String, String> validateSignUpFields(SignUpRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            errors.put("name", "Name is required");
        }
        
        if (request.getLastname() == null || request.getLastname().trim().isEmpty()) {
            errors.put("lastname", "Last name is required");
        }
        
        if (!isValidEmail(request.getEmail())) {
            errors.put("email", "Please enter a valid email address");
        }
        
        if (!isValidPassword(request.getPassword())) {
            errors.put("password", "Password must be at least 6 characters long");
        }
        
        return errors;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public ResponseEntity<EntityResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        logger.error("Validation error: {}", errors);
        return ResponseEntity.badRequest()
            .body(EntityResponse.error("Validation failed", errors));
    }
}