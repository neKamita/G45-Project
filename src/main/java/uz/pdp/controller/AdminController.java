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
 * The Admin Control Panel - Where Dreams Are Made (or Crushed) 👑
 * 
 * This controller holds the supreme power over the application.
 * With great power comes great responsibility... and a lot of support tickets.
 * 
 * Features:
 * - User management (aka playing God)
 * - Seller approval (making dreams come true)
 * - Account management (the ban hammer lives here)
 * - System monitoring (watching the chaos unfold)
 *
 * WARNING: Only the chosen ones (ADMIN role) may enter.
 * Mere mortals will be rejected faster than a JavaScript PR in a Java project.
 *
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

    // The service that does all the actual work (we just look important)
    @Autowired
    private AdminService adminService;

    /**
     * Promotes a regular user to the esteemed position of Seller.
     * 
     * Technical details:
     * - Validates user existence and eligibility
     * - Updates user role and permissions
     * - Sends notification emails
     * - Updates audit logs
     * 
     * Fun fact: 90% of users think "seller" means they can sell anything.
     * No, you can't sell your neighbor's car, Bob. 🚗
     *
     * @param userId ID of the soon-to-be-promoted user
     * @return Success message or a list of reasons why they're not worthy
     * @throws ResourceNotFoundException if the user has ascended to a higher plane of existence
     * @throws ForbiddenException if they tried to hack their way to seller status
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
     * Updates a user's profile with admin privileges.
     * Because sometimes users need a helping hand (or a stern correction).
     * 
     * Process:
     * 1. Validate update request
     * 2. Check user existence
     * 3. Apply changes
     * 4. Update audit logs
     * 
     * Note: Yes, we can reset their password.
     * No, we won't set it to "password123" even if they beg. 🔒
     *
     * @param userId The chosen one's ID
     * @param updateUserDTO Their new destiny
     * @return Updated user details or rejection notice
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