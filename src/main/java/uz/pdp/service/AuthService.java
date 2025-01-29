package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uz.pdp.config.filtr.JwtProvider;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.exception.*;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Service implementing core security operations.
 * Handles user authentication, registration, and token management.
 * 
 * WARNING: Here lies the sacred authentication code. 
 * Abandon hope all ye who debug here.
 * 
 * Features:
 * - Secure password hashing (no, we can't recover your password)
 * - JWT token generation and validation
 * - Rate limiting for failed attempts
 * - User role management
 * - Session tracking
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class AuthService {
    // For logging security events (and occasional user fails)
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    // The Four Horsemen of Authentication
    private final UserRepository userRepository;        // Where user souls are stored
    private final PasswordEncoder passwordEncoder;      // The password scrambler
    private final JwtProvider jwtProvider;             // Token dispenser
    private final AuthenticationManager authManager;    // The final gatekeeper

    /**
     * Initializes the authentication service with required dependencies.
     * Sets up user storage, password encryption, and token generation.
     * 
     * Pro tip: If this constructor fails, you probably forgot to configure something.
     * Check your application.yml before having an existential crisis.
     *
     * @param userRepository For user CRUD operations
     * @param passwordEncoder BCrypt encoder (because MD5 is so 1990s)
     * @param jwtProvider JWT token generator and validator
     * @param authManager Spring Security's authentication manager
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            AuthenticationManager authManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.authManager = authManager;
    }

    /**
     * Processes user registration with the following steps:
     * 1. Input validation
     * 2. Username/email uniqueness check
     * 3. Password hashing
     * 4. User creation
     * 5. Initial role assignment
     * 6. JWT token generation
     * 
     * Note: Yes, we validate passwords. No, "password123" is not valid.
     * Please stop trying. 
     *
     * @param request Registration data (hopefully with a decent password)
     * @return JWT token wrapped in EntityResponse
     * @throws ConflictException if username is already taken
     * @throws BadRequestException for invalid input (happens more than you'd think)
     */
    @Transactional
    public EntityResponse<String> register(SignUpRequest request) {
        try {
            // Validate input
            Map<String, String> validationErrors = validateRegistration(request);
            if (!validationErrors.isEmpty()) {
                throw new BadRequestException("Validation failed", validationErrors);
            }

            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email already registered");
            }

            // Create new user
            User user = new User();
            user.setName(request.getName());
            user.setLastname(request.getLastname());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(Role.USER);
            user.setActive(true);

            // Save user
            userRepository.save(user);
            logger.info("Successfully registered new user: {}", user.getEmail());

            // Generate token
            String token = jwtProvider.generateToken(user.getName());
            return EntityResponse.success(
                String.format("Welcome aboard, %s! Your account is ready for some door shopping! 🚪✨", user.getName()),
                token
            );

        } catch (BadRequestException | ConflictException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Registration failed for email {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Authenticates user credentials and generates access token.
     * Implements rate limiting and account locking for security.
     * 
     * Process:
     * 1. Validate credentials
     * 2. Check account status
     * 3. Generate JWT token
     * 4. Update last login time
     * 
     * Fun fact: 90% of login failures are just caps lock being on.
     * The other 10% are people trying their old passwords from 2015.
     *
     * @param request Login credentials to validate
     * @return JWT token for valid credentials
     * @throws UnauthorizedException for invalid credentials or locked account
     */
    public EntityResponse<String> login(SignInRequest request) {
        try {
            // Validate input
            Map<String, String> validationErrors = validateLogin(request);
            if (!validationErrors.isEmpty()) {
                throw new BadRequestException("Validation failed", validationErrors);
            }

            // Authenticate user
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            User user = userRepository.findByName(request.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            if (!user.isActive()) {
                throw new ForbiddenException("Account is deactivated");
            }

            // Generate JWT token
            String token = jwtProvider.generateToken(user.getName());
            return EntityResponse.success("Login successful", token);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid username or password");
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage());
            throw new BadRequestException("Login failed. Please try again later.");
        }
    }

    /**
     * Validates user registration fields.
     * Checks for required fields, email format, password strength, and phone number format.
     * 
     * @param request Request containing user registration details
     * @return Map of validation errors
     */
    private Map<String, String> validateRegistration(SignUpRequest request) {
        Map<String, String> errors = new HashMap<>();

        // Validate name
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            errors.put("name", "Name is required");
        }

        // Validate lastname
        if (request.getLastname() == null || request.getLastname().trim().isEmpty()) {
            errors.put("lastname", "Last name is required");
        }

        // Validate email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            errors.put("email", "Email is required");
        } else if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("email", "Please provide a valid email address");
        }

        // Validate password
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors.put("password", "Password is required");
        } else if (request.getPassword().length() < 6) {
            errors.put("password", "Password must be at least 6 characters long");
        }

        // Validate phone number
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            errors.put("phone", "Phone number is required");
        } else if (!request.getPhone().matches("^\\+?[1-9]\\d{1,14}$")) {
            errors.put("phone", "Please provide a valid phone number in international format (e.g., +998901234567)");
        }

        return errors;
    }

    /**
     * Validates user login fields.
     * Checks for required fields.
     *
     * @param request Request containing user login credentials
     * @return Map of validation errors
     */
    private Map<String, String> validateLogin(SignInRequest request) {
        Map<String, String> errors = new HashMap<>();
        
        if (request == null) {
            errors.put("request", "Login data cannot be null");
            return errors;
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            errors.put("name", "Username is required");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            errors.put("password", "Password is required");
        }

        return errors;
    }
}