package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/sign-in")
    @Operation(summary = "Sign in", description = "Authenticate user and return JWT token")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest signInRequest) {
        logger.info("Received sign-in request for user: {}", signInRequest.getName());
        return authService.signIn(signInRequest);
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Sign up", description = "Register a new user")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        logger.info("Received sign-up request for user: {}", signUpRequest.getName());
        return authService.signUp(signUpRequest);
    }
}