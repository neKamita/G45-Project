package uz.pdp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.Role;

/**
 * DTO for updating user information.
 * Contains only the fields that are allowed to be updated.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDTO {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Lastname is required")
    private String lastname;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number must be in international format")
    private String phone;
    
    private Role role;
    
    private boolean active;
}
