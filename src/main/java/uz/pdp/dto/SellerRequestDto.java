package uz.pdp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerRequestDto {
    @NotNull(message = "User ID cannot be null")
    private Long userId;
}
