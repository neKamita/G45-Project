package uz.pdp.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/request-seller/{userId}")
    @Operation(summary = "Request to become a seller")
    public ResponseEntity<EntityResponse<User>> requestSeller(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.requestSeller(userId));
    }

    @PostMapping("/verify-seller/{userId}/{code}")
    @Operation(summary = "Verify seller email code")
    @RateLimiter(name = "verifySellerLimit", fallbackMethod = "verifySellerFallback")
    public ResponseEntity<EntityResponse<User>> verifySellerEmail(
            @PathVariable Long userId,
            @PathVariable String code) {
        logger.info("Verifying seller email code for user ID: {}", userId);
        return ResponseEntity.ok(userService.verifySellerEmail(userId, code));
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