package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AuthService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Sign up")
    public ResponseEntity<EntityResponse<Map<String, String>>> signUp(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign in")
    public ResponseEntity<EntityResponse<Map<String, String>>> signIn(@Valid @RequestBody SignInRequest request) {
        return authService.signIn(request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EntityResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        return authService.handleValidationErrors(ex);
    }
}