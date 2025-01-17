package uz.pdp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest {
    @NotBlank(message = "Username is required")
    private String name;

    @NotBlank(message = "Password is required")
    private String password;
}