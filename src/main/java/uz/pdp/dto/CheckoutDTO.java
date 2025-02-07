package uz.pdp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.pdp.enums.ItemType;

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

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Name cannot be longer than 100 characters")
    private String customerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phoneNumber;

    @NotBlank(message = "Delivery address is required")
    @Size(max = 255, message = "Address cannot be longer than 255 characters")
    private String deliveryAddress;

    private String comment;
}
