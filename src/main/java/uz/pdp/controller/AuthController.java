package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.exception.BadRequestException;
import uz.pdp.exception.ConflictException;
import uz.pdp.exception.UnauthorizedException;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AuthService;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication Controller for managing user access and security.
 * Handles user registration, login, and token management operations.
 * 
 * WARNING: This is our security fortress. 🏰
 * One wrong change here and we're more exposed than a nudist beach.
 * 
 * Features:
 * - User registration with validation
 * - JWT-based authentication
 * - Password encryption
 * - Rate limiting
 * - Session management
 *
 * @version 1.0
 * @since 2025-01-17
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
@Slf4j
public class AuthController {
    // Logger for security events and user stupidity documentation
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    // The wizard behind our security curtain
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Processes user registration requests with the following steps:
     * 1. Validates user input
     * 2. Checks for existing users
     * 3. Encrypts password
     * 4. Creates user account
     * 5. Generates JWT token
     * 
     * Pro tip: Users will still try to register with "password123" 
     * no matter how many validation rules we add 🤦
     *
     * @param registerDto Registration details (hopefully not another 'admin/admin')
     * @return JWT token for successful registration
     * @throws BadRequestException if validation fails (it will)
     * @throws ConflictException if username is taken (it probably is)
     */
    @PostMapping("/signup")
    @Operation(summary = "Register a new user")
    public ResponseEntity<EntityResponse<String>> register(@Valid @RequestBody SignUpRequest registerDto) {
        try {
            logger.info("Processing registration request for user: {}", registerDto.getName());
            EntityResponse<String> response = authService.register(registerDto);
            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            logger.error("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(EntityResponse.error(e.getMessage()));
        } catch (ConflictException e) {
            logger.error("Username is taken: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing registration request for user {}: {}", registerDto.getName(), e.getMessage());
            return ResponseEntity.internalServerError().body(
                EntityResponse.error(e.getMessage())
            );
        }
    }

    /**
     * Authenticates users and issues JWT tokens with the following process:
     * 1. Validates credentials
     * 2. Checks account status
     * 3. Generates new JWT token
     * 4. Updates last login timestamp
     * 
     * Note: We give them 5 tries before rate limiting kicks in,
     * because apparently remembering passwords is hard. 🔑
     *
     * @param loginDto Login credentials (please don't be "admin/admin")
     * @return JWT token or a polite "nice try" message
     * @throws UnauthorizedException when they try to guess the password
     */
    @PostMapping("/signin")
    @Operation(summary = "Authenticate user and get token")
    public ResponseEntity<EntityResponse<String>> login(@Valid @RequestBody SignInRequest loginDto) {
        try {
            logger.info("Processing login request for user: {}", loginDto.getUsername());
            EntityResponse<String> response = authService.login(loginDto);
            return ResponseEntity.ok(response);
        } catch (BadRequestException | UnauthorizedException e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(EntityResponse.error("Invalid username or password"));
        } catch (Exception e) {
            logger.error("Error processing login request for user {}: {}", loginDto.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(EntityResponse.error("Invalid username or password"));
        }
    }

    /**
     * Handles validation exceptions for all authentication requests.
     * Processes field validation errors and returns appropriate error messages.
     *
     * @param ex MethodArgumentNotValidException containing validation errors
     * @return ResponseEntity with detailed validation error messages
     *         - 400 Bad Request with list of validation errors
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}