package uz.pdp.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import uz.pdp.dto.OrderDto;
import uz.pdp.entity.Order;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.exception.BadRequestException;
import uz.pdp.exception.ConflictException;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.service.OrderService;
import uz.pdp.service.UserService;
import uz.pdp.payload.EntityResponse;
import uz.pdp.dto.OrderResponseDTO;
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<EntityResponse<List<OrderResponseDTO>>> getUserOrders(@AuthenticationPrincipal User user) {
        try {
            logger.info("Retrieving orders for user ID: {}", user.getEmail());
            List<Order> orders = orderService.getUserOrders(user.getEmail());
            List<OrderResponseDTO> orderDTOs = orders.stream()
                .map(OrderResponseDTO::fromOrder)
                .collect(Collectors.toList());
            return ResponseEntity.ok(EntityResponse.success("Orders retrieved successfully", orderDTOs));
        } catch (Exception e) {
            logger.error("Error retrieving user orders: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to retrieve orders: " + e.getMessage()));
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
     * The Seller Transformation Station 
     * 
     * Handles requests from regular users who dream of becoming sellers.
     * Think of it as a digital knighting ceremony, but with more paperwork!
     *
     * @param userId ID of the aspiring merchant (must match the authenticated user)
     * @param currentUser The current user (our brave candidate)
     * @return EntityResponse containing the user's updated details or error message
     *
     * Security:
     * - Only users with USER role can request seller status
     * - Users can only request seller status for themselves
     * - Prevents duplicate requests (no double-dipping!)
     *
     * Pro tip: Check your email after making the request,
     * that's where the magic verification code lives!
     */
    @PostMapping("/request-seller/{userId}")
    @Operation(summary = "Request to become a seller")
    @PreAuthorize("hasRole('USER')")
    public EntityResponse<User> requestSeller(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        try {
            // Log the brave soul attempting to become a seller
            logger.info("Processing seller request. Requested userId: {}, Authenticated user: {} (ID: {}), Role: {}", 
                userId, currentUser.getUsername(), currentUser.getId(), currentUser.getRole());
            
            // Security Check #1: Identity Verification
            // Making sure users aren't trying to be sneaky and request for someone else
            if (!currentUser.getId().equals(userId)) {
                String errorMsg = String.format("Access denied. User '%s' cannot request seller status for user ID %d", 
                    currentUser.getUsername(), userId);
                logger.warn(errorMsg);
                return new EntityResponse<>(errorMsg, false, null);
            }
            
            // Security Check #2: Role Verification
            // Only regular users can apply - no sellers or admins allowed!
            if (currentUser.getRole() != Role.USER) {
                String errorMsg = String.format("Access denied. Only users with USER role can request seller status. Current role: %s", 
                    currentUser.getRole());
                logger.warn(errorMsg);
                return new EntityResponse<>(errorMsg, false, null);
            }
            
            // Check for Existing Request
            // Preventing the "Are we there yet?" syndrome
            if (currentUser.isSellerRequestPending()) {
                String msg = String.format("You already have a pending seller request. Please check your email (%s) for verification instructions.", 
                    currentUser.getEmail());
                logger.info(msg);
                return new EntityResponse<>(msg, false, currentUser);
                }
            
            // Launch the seller request process!
            return userService.requestSeller(userId);
        } catch (ConflictException e) {
            // Handle conflicts (like trying to become a seller twice)
            logger.warn("Conflict while processing seller request: {}", e.getMessage());
            return new EntityResponse<>(e.getMessage(), false, null);
        } catch (Exception e) {
            // Something went terribly wrong
            String errorMsg = String.format("Failed to process seller request for user %d: %s", userId, e.getMessage());
            logger.error(errorMsg, e);
            return new EntityResponse<>(errorMsg, false, null);
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
    public EntityResponse<User> verifySellerEmail(
            @PathVariable Long userId,
            @PathVariable String code) {
        try {
            logger.info("Verifying seller email code for user ID: {}", userId);
            return userService.verifySellerEmail(userId, code);
        } catch (Exception e) {
            logger.error("Error verifying seller email for user {}: {}", userId, e.getMessage());
            throw new BadRequestException("Failed to verify seller email: " + e.getMessage());
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
                    .body(EntityResponse.<List<User>>error("Failed to fetch users: " + e.getMessage(), null));
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