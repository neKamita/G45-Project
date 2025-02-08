package uz.pdp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import uz.pdp.enums.ItemType;

/**
 * DTO for requesting a price list for a specific item.
 * 
 * 💰 Because everyone loves a good price list!
 * 
 * Item types supported:
 * - DOOR: Main door products
 * - FURNITURE_ACCESSORY: Door furniture and accessories (handles, hinges, locks)
 * - MOULDING: Decorative trim pieces and frames
 */
@Data
public class PriceListRequestDto {
    @NotNull(message = "Item ID is required")
    private Long itemId;
    
    @NotNull(message = "Item type is required")
    private ItemType itemType;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    private String phone;
}
