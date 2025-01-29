package uz.pdp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.entity.BasketItem;
import uz.pdp.enums.ItemType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for checkout operations.
 * Wraps up your purchase details with a bow! 🎁
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDTO {
    private Long id;              // Basket item ID
    private String name;          // Item name
    private ItemType type;        // DOOR, ACCESSORY, or MOULDING
    private double price;         // Item price
    private int quantity;         // Quantity purchased
    private String image;         // Item image URL
    private Long orderId;         // Order ID (only for doors)
    private LocalDateTime checkoutTime = LocalDateTime.now();

    public static CheckoutResponseDTO from(BasketItem item, Long orderId) {
        CheckoutResponseDTO dto = new CheckoutResponseDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setType(item.getType());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setImage(item.getImage());
        dto.setOrderId(orderId);
        return dto;
    }
}
