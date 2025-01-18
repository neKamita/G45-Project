package uz.pdp.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import uz.pdp.dto.OrderDto;
import uz.pdp.entity.Order;
import uz.pdp.entity.User;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.service.OrderService;
import uz.pdp.service.UserService;
import uz.pdp.payload.EntityResponse;

import java.util.List;

/**
 * REST controller for managing user-related operations.
 * Provides endpoints for user profile management, order history,
 * and user preferences. Most operations require authentication.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing user profiles and related operations")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final OrderService orderService;

    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    /**
     * Retrieves the current user's profile.
     * Returns detailed user information including preferences and settings.
     *
     * @param user Currently authenticated user
     * @return ResponseEntity with user profile details
     *         - 200 OK with user details
     *         - 401 Unauthorized if not authenticated
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    @RateLimiter(name = "userProfile")
    public ResponseEntity<EntityResponse<User>> getCurrentUser(@AuthenticationPrincipal User user) {
        try {
            logger.info("Retrieving profile for user ID: {}", user.getId());
            User currentUser = userService.getCurrentUser();
            return ResponseEntity.ok(EntityResponse.success("User profile retrieved successfully", currentUser));
        } catch (Exception e) {
            logger.error("Error retrieving user profile: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to retrieve profile: " + e.getMessage()));
        }
    }

    /**
     * Updates the current user's profile.
     * Allows modification of user details and preferences.
     *
     * @param user Currently authenticated user
     * @param updatedUser Updated user details
     * @return ResponseEntity with updated user profile
     *         - 200 OK if profile updated successfully
     *         - 400 Bad Request if validation fails
     *         - 401 Unauthorized if not authenticated
     */
    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<EntityResponse<User>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody User updatedUser) {
        try {
            logger.info("Updating profile for user ID: {}", user.getId());
            EntityResponse<User> response = userService.updateProfile(user.getId(), updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating user profile: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new EntityResponse<>("Failed to update profile: " + e.getMessage(), false, null));
        }
    }

    /**
     * Retrieves the current user's order history.
     * Returns a list of all orders placed by the user.
     *
     * @param user Currently authenticated user
     * @return ResponseEntity with list of user orders
     *         - 200 OK with order history
     *         - 401 Unauthorized if not authenticated
     */
    @GetMapping("/orders")
    @Operation(summary = "Get user order history")
    public ResponseEntity<EntityResponse<List<Order>>> getUserOrders(@AuthenticationPrincipal User user) {
        try {
            logger.info("Retrieving orders for user ID: {}", user.getId());
            List<Order> orders = orderService.getUserOrders(user.getId());
            return ResponseEntity.ok(EntityResponse.success("Orders retrieved successfully", orders));
        } catch (Exception e) {
            logger.error("Error retrieving user orders: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to retrieve orders: " + e.getMessage()));
        }
    }

    /**
     * Creates a new order for the current user.
     * Validates order details before creation.
     *
     * @param user Currently authenticated user
     * @param orderDto Order details
     * @return ResponseEntity with created order
     *         - 201 Created if order created successfully
     *         - 400 Bad Request if validation fails
     *         - 401 Unauthorized if not authenticated
     */
    @PostMapping("/orders")
    @Operation(summary = "Create new order")
    public ResponseEntity<EntityResponse<Order>> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderDto orderDto) {
        try {
            logger.info("Creating order for user ID: {}", user.getId());
            EntityResponse<Order> response = orderService.createOrder(user.getId(), orderDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating order for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new EntityResponse<>("Failed to create order: " + e.getMessage(), false, null));
        }
    }

    /**
     * Deactivates the current user's account.
     * This operation requires re-authentication for security.
     *
     * @param user Currently authenticated user
     * @return ResponseEntity with deactivation status
     *         - 200 OK if account deactivated successfully
     *         - 401 Unauthorized if not authenticated
     *         - 403 Forbidden if password confirmation fails
     */
    @PostMapping("/deactivate")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<EntityResponse<Void>> deactivateAccount(@AuthenticationPrincipal User user) {
        try {
            logger.info("Deactivating account for user ID: {}", user.getId());
            EntityResponse<Void> response = userService.deactivateAccount(user.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deactivating account for user {}: {}", user.getId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new EntityResponse<>("Failed to deactivate account: " + e.getMessage(), false, null));
        }
    }

    /**
     * Requests to become a seller.
     * 
     * @param userId User ID
     * @return ResponseEntity with request status
     *         - 200 OK if request sent successfully
     *         - 400 Bad Request if validation fails
     */
    @PostMapping("/request-seller/{userId}")
    @Operation(summary = "Request to become a seller")
    public ResponseEntity<EntityResponse<User>> requestSeller(@PathVariable Long userId) {
        try {
            EntityResponse<User> response = userService.requestSeller(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error requesting seller status for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new EntityResponse<>("Failed to process seller request: " + e.getMessage(), false, null));
        }
    }

    /**
     * Verifies seller email code.
     * 
     * @param userId User ID
     * @param code   Verification code
     * @return ResponseEntity with verification status
     *         - 200 OK if verification successful
     *         - 400 Bad Request if validation fails
     */
    @PostMapping("/verify-seller/{userId}/{code}")
    @Operation(summary = "Verify seller email code")
    @RateLimiter(name = "verifySellerLimit", fallbackMethod = "verifySellerFallback")
    public ResponseEntity<EntityResponse<User>> verifySellerEmail(
            @PathVariable Long userId,
            @PathVariable String code) {
        try {
            logger.info("Verifying seller email code for user ID: {}", userId);
            EntityResponse<User> response = userService.verifySellerEmail(userId, code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error verifying seller email for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new EntityResponse<>("Failed to verify seller email: " + e.getMessage(), false, null));
        }
    }

    /**
     * Retrieves all users.
     * 
     * @return ResponseEntity with list of users
     *         - 200 OK with user list
     */
    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<EntityResponse<List<User>>> getAllUsers() {
        try {
            EntityResponse<List<User>> response = userService.getAllUsers();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching all users: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new EntityResponse<>("Failed to fetch users: " + e.getMessage(), false, null));
        }
    }

    /**
     * Retrieves a user by ID.
     * 
     * @param id User ID
     * @return ResponseEntity with user details
     *         - 200 OK with user details
     *         - 404 Not Found if user not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<EntityResponse<User>> getUserById(@PathVariable Long id) {
        try {
            EntityResponse<User> response = userService.getUserById(id);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            logger.error("User not found with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching user {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to fetch user: " + e.getMessage()));
        }
    }
}