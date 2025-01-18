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
@Slf4j
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
        } catch (BadRequestException e) {
            logger.error("Validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing registration request for user {}: {}", registerDto.getName(), e.getMessage());
            return ResponseEntity.internalServerError().body(
                EntityResponse.error("Internal server error: " + e.getMessage())
            );
        }
    }

    /**
     * Handles user authentication requests.
     * Validates credentials and generates JWT token for valid users.
     *
     * @param loginRequest SignInRequest containing login credentials
     * @return ResponseEntity with JWT token on successful authentication
     *         - 200 OK with token if authentication successful
     *         - 401 Unauthorized if credentials are invalid
     *         - 400 Bad Request if validation fails
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Authenticate user and get token")
    public ResponseEntity<EntityResponse<String>> login(@RequestBody SignInRequest loginRequest) {
        try {
            logger.info("Processing login request for user: {}", loginRequest.getUsername());
            EntityResponse<String> response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (BadRequestException e) {
            logger.error("Authentication failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing login request for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.internalServerError().body(
                EntityResponse.error("Internal server error: " + e.getMessage())
            );
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