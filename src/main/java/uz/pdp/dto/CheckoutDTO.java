package uz.pdp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.ItemType;
import uz.pdp.enums.OrderType;

import java.time.ZonedDateTime;

/**
 * DTO for handling checkout requests.
 * Where shopping carts dreams come true! 🚪✨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutDTO {
    @NotNull(message = "Item ID is required")
    private Long itemId;

    @NotNull(message = "Item type is required")
    private ItemType itemType;


    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{9}$", message = "Phone number must be exactly 9 digits")
    private String phoneNumber;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters")
    private String customerName;

    @NotNull(message = "Order type is required")
    private OrderType orderType = OrderType.FULL_SET; // Default to PURCHASE

    @Size(max = 1000, message = "Comment cannot be longer than 1000 characters")
    private String comment;
}
