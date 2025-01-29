package uz.pdp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.dto.OrderDto.OrderType;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for checking out specific items from the basket.
 * Because sometimes you just want those specific doors to find their new homes! 🚪✨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutItemsDTO {
    @NotNull(message = "Item IDs list cannot be null")
    @NotEmpty(message = "Item IDs list cannot be empty")
    private List<Long> basketItemIds;

    @NotNull(message = "Order type is required")
    private OrderType orderType;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    private ZonedDateTime preferredDeliveryTime;

    private String comment;

    private String installationNotes;

    private String deliveryNotes;
}
