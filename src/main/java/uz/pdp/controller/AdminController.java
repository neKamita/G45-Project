package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uz.pdp.payload.EntityResponse;
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
@Slf4j
@Tag(name = "Admin Control", description = "APIs for admin control and management")
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a seller request")
    public ResponseEntity<EntityResponse<Void>> approveSeller(@PathVariable Long userId) {
        try {
            log.info("Processing seller approval request for user ID: {}", userId);
            boolean isApproved = adminService.approveSeller(userId);
            
            if (isApproved) {
                log.info("Successfully approved seller request for user ID: {}", userId);
                return ResponseEntity.ok(new EntityResponse<>("User successfully approved as seller", true, null));
            } else {
                log.warn("Failed to approve seller request for user ID: {}", userId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new EntityResponse<>("Failed to approve user as seller - user may not have requested seller status", false, null));
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for seller approval - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EntityResponse<>("Invalid request: " + e.getMessage(), false, null));
        } catch (Exception e) {
            log.error("Error processing seller approval for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to process seller approval: " + e.getMessage(), false, null));
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<EntityResponse<Void>> deactivateAccount(@PathVariable Long userId) {
        try {
            log.info("Processing account deactivation request for user ID: {}", userId);
            EntityResponse<Void> response = adminService.deactivateAccount(userId);
            
            if (response.isSuccess()) {
                log.info("Successfully deactivated account for user ID: {}", userId);
            } else {
                log.warn("Failed to deactivate account for user ID: {}", userId);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for account deactivation - user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new EntityResponse<>("Invalid request: " + e.getMessage(), false, null));
        } catch (Exception e) {
            log.error("Error deactivating account for user ID {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to deactivate account: " + e.getMessage(), false, null));
        }
    }
}