package uz.pdp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.ItemType;

import java.time.LocalDateTime;

/**
 * DTO for representing checkout history entries.
 * Every door has a story, and this is where we tell it! 🚪📖
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutHistoryDTO {
    private Long id;
    private String itemName;
    private ItemType itemType;
    private Double price;
    private Integer quantity;
    private String status;
    private LocalDateTime checkoutTime;
    private String deliveryAddress;
    private String contactPhone;
}
