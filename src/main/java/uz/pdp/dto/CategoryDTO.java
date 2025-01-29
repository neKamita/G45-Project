package uz.pdp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for transferring category data between layers.
 * 
 * Like a doorbell announcing visitors, this DTO announces categories! 🔔
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    
    @NotBlank(message = "Category name cannot be empty")
    private String name;
    
    private boolean active = true;
}
