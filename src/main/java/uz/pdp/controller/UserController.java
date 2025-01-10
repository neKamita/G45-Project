package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uz.pdp.dto.SellerRequestDto;
import uz.pdp.entity.User;
import uz.pdp.repository.UserRepository;
import uz.pdp.payload.EntityResponse;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Control", description = "APIs for user operations")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/request-seller")
    @Operation(summary = "Request to become a seller")
    public ResponseEntity<EntityResponse<User>> requestSeller(@Valid @RequestBody SellerRequestDto sellerRequestDto) {
        User user = userRepository.findById(sellerRequestDto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setSellerRequestPending(true);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(EntityResponse.success("Seller request submitted successfully", savedUser));
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<EntityResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(EntityResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<EntityResponse<User>> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(EntityResponse.success("User retrieved successfully", user));
    }
}
