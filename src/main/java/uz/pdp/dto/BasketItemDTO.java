package uz.pdp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.ItemType;
import java.math.BigDecimal;

/**
 * DTO for adding items to basket
 * 
 * Fun fact: This DTO is like a shopping cart item,
 * but for doors! 🛒🚪
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemDTO {
    private Long id;
    
    @NotNull(message = "Item ID must not be null")
    private Long itemId;
    
    @NotNull(message = "Item type must not be null")
    private ItemType type;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
    
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String name;  // Item name
    private String imageUrl;  // URL to the item's image

    // Constructor for backward compatibility
    public BasketItemDTO(Long itemId, ItemType type, int quantity) {
        this.itemId = itemId;
        this.type = type;
        this.quantity = quantity;
    }
}
