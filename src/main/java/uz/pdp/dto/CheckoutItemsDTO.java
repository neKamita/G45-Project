package uz.pdp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for checking out specific items from the basket.
 * Because sometimes you just want to buy those fancy door handles! 🚪✨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutItemsDTO {
    @NotNull(message = "Item IDs list cannot be null")
    @NotEmpty(message = "Item IDs list cannot be empty")
    private List<Long> basketItemIds;
}
