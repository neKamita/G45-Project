package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final AuthenticationManager authenticationManager;

    /**
     * Constructor for AuthService.
     * Initializes dependencies for user repository, password encoder, JWT service, and authentication manager.
     *
     * @param userRepository User repository for database operations
     * @param passwordEncoder Password encoder for secure password storage
     * @param jwtProvider JWT provider for token generation
     * @param authenticationManager Authentication manager for user authentication
     */
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Processes user registration requests.
     * Validates input, checks for existing users, and creates a new user account.
     *
     * @param registerDto Request containing user registration details
     * @return EntityResponse containing JWT token if registration successful
     * @throws BadRequestException if validation fails or user already exists
     */
    @Transactional
    public EntityResponse<String> register(SignUpRequest registerDto) {
        try {
            // Validate input
            Map<String, String> validationErrors = validateRegistration(registerDto);
            if (!validationErrors.isEmpty()) {
                throw new BadRequestException("Validation failed", validationErrors);
            }

            // Check for existing user
            if (userRepository.existsByName(registerDto.getName())) {
                throw new ConflictException("An account with this username already exists");
            }

            if (userRepository.existsByEmail(registerDto.getEmail())) {
                throw new ConflictException("An account with this email address already exists");
            }

            // Create new user
            User user = new User();
            user.setName(registerDto.getName());
            user.setLastname(registerDto.getLastname());
            user.setEmail(registerDto.getEmail());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
            user.setRole(Role.USER);
            user.setActive(true);

            userRepository.save(user);
            
            // Generate JWT token
            String token = jwtProvider.generateToken(user.getName());
            return EntityResponse.success("Account created successfully", token);
            
        } catch (BadRequestException | ConflictException e) {
            logger.error("Registration validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during registration: {}", e.getMessage(), e);
            throw new BadRequestException("Unable to complete registration. Please try again later.");
        }
    }

    /**
     * Processes user login requests.
     * Validates input, authenticates user, and generates JWT token.
     *
     * @param loginDto Request containing user login credentials
     * @return EntityResponse containing JWT token if login successful
     * @throws UnauthorizedException if authentication fails
     */
    public EntityResponse<String> login(SignInRequest loginDto) {
        try {
            // Validate input
            Map<String, String> validationErrors = validateLogin(loginDto);
            if (!validationErrors.isEmpty()) {
                throw new BadRequestException("Validation failed", validationErrors);
            }

            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

            User user = userRepository.findByName(loginDto.getUsername())
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
     * @param registerDto Request containing user registration details
     * @return Map of validation errors
     */
    private Map<String, String> validateRegistration(SignUpRequest registerDto) {
        Map<String, String> errors = new HashMap<>();
        
        if (registerDto == null) {
            errors.put("registerDto", "Registration data cannot be null");
            return errors;
        }

        if (registerDto.getName() == null || registerDto.getName().trim().isEmpty()) {
            errors.put("name", "Please enter your name");
        } else if (registerDto.getName().length() < 3 || registerDto.getName().length() > 50) {
            errors.put("name", "Name must be between 3 and 50 characters");
        }

        if (registerDto.getLastname() == null || registerDto.getLastname().trim().isEmpty()) {
            errors.put("lastname", "Please enter your last name");
        } else if (registerDto.getLastname().length() < 2 || registerDto.getLastname().length() > 50) {
            errors.put("lastname", "Last name must be between 2 and 50 characters");
        }

        if (registerDto.getEmail() == null || registerDto.getEmail().trim().isEmpty()) {
            errors.put("email", "Please enter your email address");
        } else if (!registerDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.put("email", "Please enter a valid email address");
        }

        if (registerDto.getPassword() == null || registerDto.getPassword().trim().isEmpty()) {
            errors.put("password", "Please enter a password");
        } else if (registerDto.getPassword().length() < 6) {
            errors.put("password", "Password must be at least 6 characters long");
        }

        return errors;
    }

    /**
     * Validates user login fields.
     * Checks for required fields.
     *
     * @param loginDto Request containing user login credentials
     * @return Map of validation errors
     */
    private Map<String, String> validateLogin(SignInRequest loginDto) {
        Map<String, String> errors = new HashMap<>();
        
        if (loginDto == null) {
            errors.put("loginDto", "Login data cannot be null");
            return errors;
        }

        if (loginDto.getUsername() == null || loginDto.getUsername().trim().isEmpty()) {
            errors.put("name", "Username is required");
        }

        if (loginDto.getPassword() == null || loginDto.getPassword().trim().isEmpty()) {
            errors.put("password", "Password is required");
        }

        return errors;
    }
}