package uz.pdp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

/**
 * DTO for creating and updating orders.
 * 
 * Fun fact: This DTO is like a door's travel itinerary! 🗺️
 * Complete with VIP treatment and special delivery instructions! 
 */
@Data
@Getter
@Setter
public class OrderDto {
    @NotNull(message = "Door ID is required")
    private Long doorId;
    
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    private String customerName;
    private String deliveryAddress;
    private String contactPhone;
    private String email;
    
    // Using ZonedDateTime to handle timezone information properly
    private ZonedDateTime preferredDeliveryTime;
    
    // Additional order details
    private String comment;
    private String installationNotes;
    private String deliveryNotes;
    
    public enum OrderType {
        FULL_SET,    // Complete door set
        POLOTNO     // Door canvas only
    }
}
