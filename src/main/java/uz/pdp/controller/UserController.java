package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.pdp.dto.SellerRequestDto;
import uz.pdp.entity.User;
import uz.pdp.repository.UserRepository;


@RestController
@RequestMapping("/api/user")
@Tag(name = "User Control", description = "APIs for user operations")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/request-seller")
    @Operation(summary = "Request to become a seller")
    public ResponseEntity<?> requstSeller(@Valid @RequestBody SellerRequestDto sellerRequestDto) {
        User user = userRepository.findById(sellerRequestDto.getUserId()).orElseThrow(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        user.setSellerRequestPending(true);
        userRepository.save(user);
        return ResponseEntity.ok("seller request submitted successfully");

    }
}
