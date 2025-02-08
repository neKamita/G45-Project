package uz.pdp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uz.pdp.enums.ItemType;
import uz.pdp.enums.OrderType;

import java.time.ZonedDateTime;

/**
 * DTO for creating and updating orders.
 * 
 * Every order starts its journey here! 🛍️
 * From a simple request to a happy delivery! ✨
 * 
 * Key Features:
 * - Item details (ID, type, name, price)
 * - Customer information
 * - Delivery preferences
 * - Special instructions
 * 
 * @version 1.0
 * @since 2025-02-08
 */
@Data
@Getter
@Setter
public class OrderDto {
    @NotNull(message = "Item ID is required")
    private Long itemId;
    
    @NotNull(message = "Item type is required")
    private ItemType itemType;

    @NotNull(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be positive")
    private Double price;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    @NotNull(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    @NotNull(message = "Contact phone is required")
    private String contactPhone;

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    // Using ZonedDateTime to handle timezone information properly
    private ZonedDateTime preferredDeliveryTime;
    
    // Additional order details
    private String comment;
    private String installationNotes;
    private String deliveryNotes;
}
