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
import uz.pdp.enums.*;
import uz.pdp.exception.BadRequestException;
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
     * Reviews and processes a seller application request.
     * 
     * Technical Process:
     * 1. 📧 Verifies email verification is complete
     * 2. 🔍 Validates user eligibility
     * 3. 👑 Updates role if approved
     * 4. 📨 Sends appropriate notification
     * 
     * Fun fact: Some users think being a seller means they can sell doors 
     * made of chocolate. We had to add that to the FAQ. 🍫🚪
     *
     * @param userId ID of the aspiring door merchant
     * @param approved true to approve, false to reject the request
     * @return Success message or detailed rejection reasons
     * @throws ResourceNotFoundException if the user has vanished into thin air
     * @throws BadRequestException if email verification is pending
     * @throws ForbiddenException if they're trying to sneak through the back door
     */
    @PostMapping("/process-seller/{userId}")
    @Operation(summary = "Process a seller request (approve/reject)")
    public ResponseEntity<EntityResponse<Void>> processSellerRequest(
            @PathVariable Long userId,
            @RequestParam boolean approved) {
        try {
            log.info("Processing seller {} request for user ID: {}", 
                    approved ? "approval" : "rejection", userId);
            EntityResponse<Void> response = adminService.processSellerRequest(userId, approved);
            log.info("Successfully processed seller request for user ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("User not found for seller request - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error("User not found: " + e.getMessage())
            );
        } catch (BadRequestException e) {
            log.error("Invalid seller request - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error(e.getMessage())
            );
        } catch (ForbiddenException e) {
            log.error("Forbidden operation for seller request - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error("Operation not allowed: " + e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error processing seller request for user ID {}: {}", userId, e.getMessage());
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

    /**
     * Changes a user's role - the ultimate power move! 👑
     * 
     * Technical details:
     * - Validates user existence and role compatibility
     * - Handles role-specific cleanup (e.g., deactivating seller listings)
     * - Sends notification emails
     * - Updates audit logs
     * 
     * Fun fact: Some users think being an admin means they can 
     * make doors appear out of thin air. If only! 🪄🚪
     *
     * @param userId ID of the user getting the role makeover
     * @param newRole The role they're destined to become
     * @return Success message or a list of why we can't grant their wish
     * @throws ResourceNotFoundException if the user has ghosted us
     * @throws ForbiddenException if they're trying something sneaky
     * @throws BadRequestException if the role change doesn't make sense
     */
    @PostMapping("/change-role/{userId}")
    @Operation(summary = "Change a user's role")
    public ResponseEntity<EntityResponse<Void>> changeUserRole(
            @PathVariable Long userId,
            @RequestParam Role newRole) {
        try {
            log.info("Processing role change request for user ID: {} to role: {}", userId, newRole);
            EntityResponse<Void> response = adminService.changeUserRole(userId, newRole);
            log.info("Successfully changed role for user ID: {}", userId);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            log.error("User not found for role change - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error("User not found: " + e.getMessage())
            );
        } catch (ForbiddenException e) {
            log.error("Forbidden operation for role change - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error("Operation not allowed: " + e.getMessage())
            );
        } catch (BadRequestException e) {
            log.error("Invalid role change request - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                EntityResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error processing role change for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                EntityResponse.error("Internal server error: " + e.getMessage())
            );
        }
    }
}