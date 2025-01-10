package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.dto.SellerRequestDto;
import uz.pdp.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Control", description = "APIs for admin control and management")
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    @Autowired
    private AdminService adminService;

    @PostMapping("/approve-seller")
    @Operation(summary = "Approve a seller request")
    public ResponseEntity<?> approveSeller(@Valid @RequestBody SellerRequestDto sellerRequestDto) {
        boolean isApproved = adminService.approveSeller(sellerRequestDto);
        if (isApproved) {
            return ResponseEntity.ok("User approved as seller");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to approve user");
        }
    }
}
