package uz.pdp.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uz.pdp.dto.SellerRequestDto;
import uz.pdp.dto.VerificationRequest;
import uz.pdp.entity.User;
import uz.pdp.service.UserService;
import uz.pdp.payload.EntityResponse;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Control", description = "APIs for user operations")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    Logger logger = LoggerFactory.getLogger(UserController.class);




    @PostMapping("/request-seller")
    @Operation(summary = "Request to become a seller")
    public ResponseEntity<EntityResponse<User>> requestSeller(@Valid @RequestBody SellerRequestDto sellerRequestDto) {
        return ResponseEntity.ok(userService.requestSeller(sellerRequestDto));
    }

    @PostMapping("/verify-seller")
    @Operation(summary = "Verify seller email code")
    @RateLimiter(name = "verifySellerLimit", fallbackMethod = "verifySellerFallback")
    public ResponseEntity<EntityResponse<User>> verifySellerEmail(
            @Valid @RequestBody VerificationRequest request) {
        logger.info("Verifying seller email code for user ID: {}", request.getUserId());
        return ResponseEntity.ok(userService.verifySellerEmail(request.getUserId(), request.getCode()));
    }

    private ResponseEntity<EntityResponse<User>> verifySellerFallback(VerificationRequest request, Exception e) {
        logger.warn("Rate limit exceeded for seller verification: {}", request.getUserId());
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .body(EntityResponse.error("Too many verification attempts. Please try again later."));
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<EntityResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<EntityResponse<User>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
