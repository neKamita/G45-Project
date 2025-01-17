package uz.pdp.service;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for handling user authentication operations.
 * Manages user registration, authentication, and validation processes.
 * Implements security measures including password encryption and JWT token generation.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * Constructor for AuthService.
     * Initializes dependencies for user repository, password encoder, and JWT provider.
     *
     * @param userRepository User repository for database operations
     * @param passwordEncoder Password encoder for secure password storage
     * @param jwtProvider JWT provider for token generation
     */
    public AuthService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder, 
                      JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    /**
     * Processes user sign-in requests.
     * Validates credentials and generates JWT token for authenticated users.
     *
     * @param signInRequest Request containing user credentials
     * @return EntityResponse containing JWT token if authentication successful
     * @throws IllegalArgumentException if credentials are invalid
     */
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

    /**
     * Processes user registration requests.
     * Creates new user account after validating input and checking for existing users.
     *
     * @param signUpRequest Request containing user registration details
     * @return EntityResponse containing JWT token if registration successful
     * @throws IllegalArgumentException if validation fails or user already exists
     */
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

    /**
     * Validates user registration fields.
     * Checks email format, password strength, and required fields.
     *
     * @param request SignUpRequest to validate
     * @return Map of validation errors
     */
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

    /**
     * Validates email format using regex pattern.
     *
     * @param email Email to validate
     * @return boolean indicating if email is valid
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Validates password strength.
     * Password must be at least 6 characters long.
     *
     * @param password Password to validate
     * @return boolean indicating if password meets requirements
     */
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Handles validation errors from request validation.
     * Processes field errors and returns appropriate error messages.
     *
     * @param ex MethodArgumentNotValidException containing validation errors
     * @return ResponseEntity with validation error details
     */
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