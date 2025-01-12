package uz.pdp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationRequest {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Verification code is required")
    private String code;
}