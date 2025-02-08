package uz.pdp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.OrderType;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * DTO for checking out specific items from the basket.
 * Let's get those items to their new homes! 🛍️✨
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

    @NotNull(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Contact phone is required")
    private String contactPhone;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    private ZonedDateTime preferredDeliveryTime;

    private String comment;

    private String installationNotes;

    private String deliveryNotes;
}
