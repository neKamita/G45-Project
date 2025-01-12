package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uz.pdp.dto.SellerRequestDto;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@Slf4j
@Tag(name = "Admin Control", description = "APIs for admin control and management")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private AdminService adminService;

    @PostMapping("/approve-seller/{userId}")
    @Operation(summary = "Approve a seller request")
    public ResponseEntity<EntityResponse<Void>> approveSeller(@PathVariable Long userId) {
        boolean isApproved = adminService.approveSeller(userId);
        if (isApproved) {
            return ResponseEntity.ok(EntityResponse.success("User approved as seller"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EntityResponse.error("Failed to approve user"));
        }
    }

    @PostMapping("/deactivate-account/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user account")
    public ResponseEntity<EntityResponse<Void>> deactivateAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(adminService.deactivateAccount(userId));
    }
}
