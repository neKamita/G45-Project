package uz.pdp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.OrderType;

import java.time.ZonedDateTime;

/**
 * DTO for basket checkout process.
 * Contains only delivery-related information since user details
 * are taken from the authenticated user.
 * 
 * Fun fact: This DTO is like a delivery instruction manual,
 * making sure your doors arrive at the right place, right time! 🚚
 */
@Data
@NoArgsConstructor
public class BasketCheckoutDto {
    
    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;
    
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    @NotNull(message = "Preferred delivery time is required")
    private ZonedDateTime preferredDeliveryTime;
    
    // Optional fields for additional delivery information
    private String comment;
    private String installationNotes;
    private String deliveryNotes;
}
