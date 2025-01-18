package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AuthService;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling user authentication operations.
 * Provides endpoints for user registration and authentication.
 * All endpoints are public and do not require authentication.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Handles user registration requests.
     * Creates a new user account with the provided details after validation.
     * Password is encrypted before storage.
     *
     * @param registerDto SignUpRequest containing user registration details
     * @return ResponseEntity with JWT token on successful registration
     *         - 200 OK with token if registration successful
     *         - 400 Bad Request if validation fails
     *         - 409 Conflict if username/email already exists
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Register a new user")
    public ResponseEntity<EntityResponse<String>> register(@Valid @RequestBody SignUpRequest registerDto) {
        try {
            logger.info("Processing registration request for user: {}", registerDto.getName());
            EntityResponse<String> response = authService.register(registerDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing registration request for user {}: {}", registerDto.getName(),
                    e.getMessage());
            throw e;
        }
    }

    /**
     * Handles user authentication requests.
     * Validates credentials and generates JWT token for valid users.
     *
     * @param loginDto SignInRequest containing login credentials
     * @return ResponseEntity with JWT token on successful authentication
     *         - 200 OK with token if authentication successful
     *         - 401 Unauthorized if credentials are invalid
     *         - 400 Bad Request if validation fails
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Authenticate user")
    public ResponseEntity<EntityResponse<String>> login(@Valid @RequestBody SignInRequest loginDto) {
        try {
            logger.info("Processing login request for user: {}", loginDto.getName());
            return ResponseEntity.ok(authService.login(loginDto));
        } catch (Exception e) {
            logger.error("Error processing login request for user {}: {}", loginDto.getName(), e.getMessage());
            throw e;
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
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EntityResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        logger.error("Validation error: {}", errors);
        return ResponseEntity.badRequest()
                .body(EntityResponse.error("Validation failed", errors));
    }
}