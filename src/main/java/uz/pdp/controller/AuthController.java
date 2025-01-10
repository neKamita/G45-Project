package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;


    @PostMapping("/sign-up")
    @Operation(summary = "Sign up")
    public ResponseEntity<EntityResponse<Object>> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            ResponseEntity<?> response = authService.signUp(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.success("User registered successfully", response.getBody()));
        } catch (Exception e) {
            logger.error("Error during sign up: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign in")
    public ResponseEntity<EntityResponse<Object>> signIn(@Valid @RequestBody SignInRequest request) {
        try {
            logger.info("Received sign-in request for user: {}", request.getName());
            ResponseEntity<?> response = authService.signIn(request);
            return ResponseEntity.ok(EntityResponse.success("Successfully signed in", response.getBody()));
        } catch (Exception e) {
            logger.error("Error during sign in: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(EntityResponse.error(e.getMessage()));
        }
    }
}