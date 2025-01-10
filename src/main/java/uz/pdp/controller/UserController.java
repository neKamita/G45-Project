package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uz.pdp.dto.SellerRequestDto;
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

    @PostMapping("/request-seller")
    @Operation(summary = "Request to become a seller")
    public ResponseEntity<EntityResponse<User>> requestSeller(@Valid @RequestBody SellerRequestDto sellerRequestDto) {
        return ResponseEntity.ok(userService.requestSeller(sellerRequestDto));
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
