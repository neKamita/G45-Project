package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.entity.User;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.exception.ForbiddenException;
import uz.pdp.payload.EntityResponse;
import uz.pdp.dto.UpdateUserDTO;
import uz.pdp.service.AdminService;

/**
 * REST controller for handling administrative operations in the system.
 * This controller provides endpoints for admin-specific operations such as
 * seller approval and account management. All endpoints require ADMIN role.
 *
 * @author Your Team Name
 * @version 1.0
 * @since 2025-01-17
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Management", description = "APIs for administrative operations")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Approves a user's request to become a seller.
     * This endpoint processes seller approval requests and updates the user's role
     * if all requirements are met.
     *
     * @param userId The ID of the user to be approved as a seller
     * @return ResponseEntity containing the result of the approval process
     *         - 200 OK if approved successfully
     *         - 400 Bad Request if the request is invalid
     *         - 500 Internal Server Error if there's a system error
     */
    @PostMapping("/approve-seller/{userId}")
    @Operation(summary = "Approve a seller request")
    public ResponseEntity<EntityResponse<Void>> approveSeller(@PathVariable Long userId) {
        try {
            log.info("Processing seller approval request for user ID: {}", userId);
            EntityResponse<Void> response = adminService.approveSeller(userId);
            log.info("Successfully approved seller request for user ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("User not found for seller approval - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error("User not found: " + e.getMessage())
            );
        } catch (ForbiddenException e) {
            log.error("Forbidden operation for seller approval - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error("Operation not allowed: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error processing seller approval for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                EntityResponse.error("Internal server error: " + e.getMessage())
            );
        }
    }

    /**
     * Deactivates a user account.
     * This endpoint handles user account deactivation requests. When an account is deactivated:
     * - The user can no longer log in
     * - Existing data is preserved
     * - The account can be reactivated by an admin if needed
     *
     * @param userId The ID of the user account to deactivate
     * @return ResponseEntity containing the result of the deactivation process
     *         - 200 OK if deactivated successfully
     *         - 400 Bad Request if the request is invalid
     *         - 500 Internal Server Error if there's a system error
     */
    @PostMapping("/deactivate-account/{userId}")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<EntityResponse<Void>> deactivateAccount(@PathVariable Long userId) {
        try {
            log.info("Processing account deactivation request for user ID: {}", userId);
            EntityResponse<Void> response = adminService.deactivateAccount(userId);
            log.info("Successfully deactivated account for user ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("User not found for deactivation - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error("User not found: " + e.getMessage())
            );
        } catch (ForbiddenException e) {
            log.error("Forbidden operation for deactivation - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error("Operation not allowed: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error processing deactivation for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                EntityResponse.error("Internal server error: " + e.getMessage())
            );
        }
    }

    /**
     * Updates a user's profile information.
     * This endpoint allows admins to modify user details.
     *
     * @param userId ID of the user to update
     * @param updateUserDTO Updated user details
     * @return ResponseEntity containing the result of the update process
     *         - 200 OK if updated successfully
     *         - 400 Bad Request if the request is invalid
     *         - 404 Not Found if user doesn't exist
     */
    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<EntityResponse<User>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            log.info("Admin updating user profile for ID: {}", userId);
            EntityResponse<User> response = adminService.updateUser(userId, updateUserDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new EntityResponse<>("Failed to update user: " + e.getMessage(), false, null));
        }
    }
}