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
     * @param request SignUpRequest containing user registration details
     * @return ResponseEntity with JWT token on successful registration
     *         - 200 OK with token if registration successful
     *         - 400 Bad Request if validation fails
     *         - 409 Conflict if username/email already exists
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Sign up")
    public ResponseEntity<EntityResponse<Map<String, String>>> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            logger.info("Processing sign-up request for user: {}", request.getName());
            EntityResponse<Map<String, String>> response = authService.signUp(request);
            return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
        } catch (Exception e) {
            logger.error("Error processing sign-up request for user {}: {}", request.getName(), e.getMessage());
            Map<String, String> errorData = new HashMap<>();
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to process sign-up request: " + e.getMessage(), errorData));
        }
    }

    /**
     * Handles user authentication requests.
     * Validates credentials and generates JWT token for valid users.
     *
     * @param request SignInRequest containing login credentials
     * @return ResponseEntity with JWT token on successful authentication
     *         - 200 OK with token if authentication successful
     *         - 401 Unauthorized if credentials are invalid
     *         - 400 Bad Request if validation fails
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Sign in")
    public ResponseEntity<EntityResponse<Map<String, String>>> signIn(@Valid @RequestBody SignInRequest request) {
        try {
            logger.info("Processing sign-in request for user: {}", request.getName());
            return authService.signIn(request);
        } catch (Exception e) {
            logger.error("Error processing sign-in request for user {}: {}", request.getName(), e.getMessage());
            Map<String, String> errorData = new HashMap<>();
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to process sign-in request: " + e.getMessage(), errorData));
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
        logger.warn("Validation error occurred: {}", ex.getMessage());
        return authService.handleValidationErrors(ex);
    }
}